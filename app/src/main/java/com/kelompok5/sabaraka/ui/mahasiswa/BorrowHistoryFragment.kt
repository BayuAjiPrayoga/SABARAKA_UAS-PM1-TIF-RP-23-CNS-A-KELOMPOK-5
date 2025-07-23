package com.kelompok5.sabaraka.ui.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.kelompok5.sabaraka.databinding.FragmentBorrowHistoryBinding
import com.kelompok5.sabaraka.ui.BorrowRequestViewModel
import com.kelompok5.sabaraka.ui.adapter.BorrowHistoryAdapter

class BorrowHistoryFragment : Fragment() {
    private var _binding: FragmentBorrowHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BorrowRequestViewModel
    private lateinit var adapter: BorrowHistoryAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorrowHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[BorrowRequestViewModel::class.java]
        setupRecyclerView()
        observeData()

        // Setup refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        // Load data for current user
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data ketika fragment kembali aktif untuk memastikan status ter-update
        refreshData()
    }

    private fun refreshData() {
        auth.currentUser?.uid?.let { userId ->
            viewModel.loadUserBorrowRequests(userId)
        } ?: run {
            Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = BorrowHistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.borrowRequests.observe(viewLifecycleOwner) { requests ->
            adapter.submitList(requests)
            binding.swipeRefresh.isRefreshing = false

            if (requests.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!binding.swipeRefresh.isRefreshing) {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
