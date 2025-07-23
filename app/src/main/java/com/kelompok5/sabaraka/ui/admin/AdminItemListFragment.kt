package com.kelompok5.sabaraka.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok5.sabaraka.R
import com.kelompok5.sabaraka.databinding.FragmentAdminItemListBinding
import com.kelompok5.sabaraka.ui.ItemViewModel
import com.kelompok5.sabaraka.ui.adapter.AdminItemAdapter

class AdminItemListFragment : Fragment() {
    private var _binding: FragmentAdminItemListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ItemViewModel
    private lateinit var adapter: AdminItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ItemViewModel::class.java]
        setupRecyclerView()
        setupSearchFeature()
        setupUI()
        observeData()

        // Load data saat fragment dimulai
        viewModel.loadItems()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat fragment kembali aktif
        viewModel.loadItems()
    }

    private fun setupUI() {
        binding.apply {
            fabAddItem.setOnClickListener {
                findNavController().navigate(R.id.action_adminItemListFragment_to_addItemFragment)
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.refreshItems()
            }

            // Observer untuk loading state
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                swipeRefreshLayout.isRefreshing = isLoading
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            // Observer untuk error message
            viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
                if (message.isNotEmpty()) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminItemAdapter(
            onEditClick = { item ->
                val action = AdminItemListFragmentDirections.actionAdminItemListFragmentToEditItemFragment()
                val bundle = Bundle().apply {
                    putString("itemId", item.id) // Menggunakan nama argumen yang sesuai
                }
                findNavController().navigate(action.actionId, bundle)
            },
            onDeleteClick = { item ->
                showDeleteConfirmationDialog(item)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AdminItemListFragment.adapter
        }
    }

    private fun setupSearchFeature() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterItems(s?.toString() ?: "")
            }
        })
    }

    private fun filterItems(query: String) {
        val items = viewModel.items.value ?: return
        val filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.category?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.submitList(filteredItems)
        updateEmptyState(filteredItems.isEmpty())
        updateStatistics(filteredItems)
    }

    private fun observeData() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            updateEmptyState(items.isEmpty())
            updateStatistics(items)

            // Stop refreshing jika aktif
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showDeleteConfirmationDialog(item: com.kelompok5.sabaraka.model.Item) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Hapus Barang")
            .setMessage("Apakah Anda yakin ingin menghapus ${item.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteItem(item)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteItem(item: com.kelompok5.sabaraka.model.Item) {
        viewModel.deleteItem(item.id)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun updateStatistics(items: List<com.kelompok5.sabaraka.model.Item>) {
        binding.apply {
            val totalItems = items.size
            val availableItems = items.count { it.availableStock > 0 }

            tvTotalItems.text = getString(R.string.total_items_format, totalItems)
            tvAvailableItems.text = getString(R.string.available_items_format, availableItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}