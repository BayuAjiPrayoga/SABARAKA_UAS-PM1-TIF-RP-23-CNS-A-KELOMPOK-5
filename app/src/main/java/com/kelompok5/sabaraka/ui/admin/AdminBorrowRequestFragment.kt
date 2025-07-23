package com.kelompok5.sabaraka.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok5.sabaraka.databinding.FragmentAdminBorrowRequestBinding
import com.kelompok5.sabaraka.ui.BorrowRequestViewModel
import com.kelompok5.sabaraka.ui.ItemViewModel
import com.kelompok5.sabaraka.ui.adapter.AdminBorrowRequestAdapter

class AdminBorrowRequestFragment : Fragment() {
    private var _binding: FragmentAdminBorrowRequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var borrowRequestViewModel: BorrowRequestViewModel
    private lateinit var itemViewModel: ItemViewModel
    private lateinit var adapter: AdminBorrowRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBorrowRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        borrowRequestViewModel = ViewModelProvider(this)[BorrowRequestViewModel::class.java]
        itemViewModel = ViewModelProvider(requireActivity())[ItemViewModel::class.java]
        setupRecyclerView()
        setupSearchView()
        observeData()

        borrowRequestViewModel.loadAllBorrowRequests()

        binding.swipeRefresh.setOnRefreshListener {
            borrowRequestViewModel.loadAllBorrowRequests()
        }
    }

    override fun onResume() {
        super.onResume()
        borrowRequestViewModel.loadAllBorrowRequests()
    }

    private fun setupRecyclerView() {
        adapter = AdminBorrowRequestAdapter(
            onApproveClick = { request ->
                borrowRequestViewModel.updateBorrowRequestStatus(request.id, "approved")
                Toast.makeText(context, "Peminjaman disetujui", Toast.LENGTH_SHORT).show()
            },
            onRejectClick = { request ->
                borrowRequestViewModel.updateBorrowRequestStatus(request.id, "rejected")
                Toast.makeText(context, "Peminjaman ditolak", Toast.LENGTH_SHORT).show()
            },
            onReturnClick = { request ->
                Log.d("AdminBorrowRequestFragment", "Returning item, requestId: ${request.id}, itemId: ${request.itemId}, quantity: ${request.quantity}")
                if (request.itemId.isEmpty() || request.quantity <= 0) {
                    Toast.makeText(context, "Data item atau jumlah tidak valid", Toast.LENGTH_SHORT).show()
                    return@AdminBorrowRequestAdapter // Menghapus @onReturnClick dan menggunakan return sederhana
                }
                itemViewModel.returnItem(request.id, request.itemId, request.quantity)
                Toast.makeText(context, "Barang dikembalikan", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                borrowRequestViewModel.searchBorrowRequests(query)
            }
        })
    }

    private fun observeData() {
        borrowRequestViewModel.filteredRequests.observe(viewLifecycleOwner) { requests ->
            requests.forEach { request ->
                Log.d("AdminBorrowRequestFragment", "Received request: userId=${request.userId}, userName=${request.userName}, userNim=${request.userNim}")
            }
            adapter.submitList(requests)
            binding.swipeRefresh.isRefreshing = false

            updateCounters(requests)

            if (requests.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        borrowRequestViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!binding.swipeRefresh.isRefreshing) {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        borrowRequestViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        itemViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && !borrowRequestViewModel.isLoading.value!!) {
                binding.progressBar.visibility = View.VISIBLE
            } else if (!isLoading && !borrowRequestViewModel.isLoading.value!!) {
                binding.progressBar.visibility = View.GONE
            }
        }

        itemViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCounters(requests: List<com.kelompok5.sabaraka.model.BorrowRequest>) {
        val pendingCount = requests.count { it.status == "pending" }
        val approvedCount = requests.count { it.status == "approved" }

        binding.tvPendingRequests.text = String.format("%d", pendingCount)
        binding.tvApprovedRequests.text = String.format("%d", approvedCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}