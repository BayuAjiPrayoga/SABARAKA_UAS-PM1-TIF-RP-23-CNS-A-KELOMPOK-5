package com.kelompok5.sabaraka.ui.mahasiswa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.kelompok5.sabaraka.R
import com.kelompok5.sabaraka.databinding.FragmentItemListBinding
import com.kelompok5.sabaraka.ui.ItemViewModel
import com.kelompok5.sabaraka.ui.adapter.ItemAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import android.text.TextWatcher
import android.text.Editable

class ItemListFragment : Fragment() {
    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ItemViewModel
    private lateinit var adapter: ItemAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[ItemViewModel::class.java]
        setupRecyclerView()
        setupUI()
        observeData()

        // Load data saat fragment dimulai
        viewModel.loadItems()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat fragment kembali aktif
        viewModel.refreshItems()
    }

    private fun setupUI() {
        // Setup swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshItems()
        }
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter { item ->
            val userId = auth.currentUser?.uid
            if (userId != null) {
                if (item.availableStock > 0) {
                    showQuantityDialog(item, userId)
                } else {
                    Toast.makeText(context, "Barang kosong - ${item.name} tidak dapat dipinjam", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun showQuantityDialog(item: com.kelompok5.sabaraka.model.Item, userId: String) {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_borrow_item, null)

        // Create modern dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Make dialog background transparent for rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Initialize views with proper error handling
        try {
            val tvItemName = dialogView.findViewById<TextView>(R.id.tvItemName)
            val tvAvailableStock = dialogView.findViewById<TextView>(R.id.tvAvailableStock)
            val tvTotalStock = dialogView.findViewById<TextView>(R.id.tvTotalStock)
            val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.etQuantity)
            val btnDecrease = dialogView.findViewById<MaterialButton>(R.id.btnDecrease)
            val btnIncrease = dialogView.findViewById<MaterialButton>(R.id.btnIncrease)
            val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
            val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
            val btnBorrow = dialogView.findViewById<MaterialButton>(R.id.btnBorrow)

            // Set item data
            tvItemName.text = item.name
            tvAvailableStock.text = "Tersedia: ${item.availableStock}"
            tvTotalStock.text = "Total: ${item.stock}"
            etQuantity.setText("1")

            // Flag to prevent infinite loop in TextWatcher
            var isUpdatingFromCode = false

            // Helper function to update quantity
            fun updateQuantity(newQuantity: Int) {
                val validQuantity = when {
                    newQuantity < 1 -> 1
                    newQuantity > item.availableStock -> item.availableStock
                    else -> newQuantity
                }

                // Set flag to prevent TextWatcher from triggering
                isUpdatingFromCode = true
                etQuantity.setText(validQuantity.toString())
                etQuantity.setSelection(validQuantity.toString().length)
                isUpdatingFromCode = false

                // Update button states
                btnDecrease.isEnabled = validQuantity > 1
                btnIncrease.isEnabled = validQuantity < item.availableStock

                // Update borrow button text
                btnBorrow.text = "Pinjam $validQuantity Barang"
            }

            // Set initial state
            updateQuantity(1)

            // Button click listeners
            btnDecrease.setOnClickListener {
                try {
                    val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 1
                    updateQuantity(currentQuantity - 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error mengubah jumlah", Toast.LENGTH_SHORT).show()
                }
            }

            btnIncrease.setOnClickListener {
                try {
                    val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 1
                    updateQuantity(currentQuantity + 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error mengubah jumlah", Toast.LENGTH_SHORT).show()
                }
            }

            // Text change listener for manual input
            etQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    // Only update if the change is not from code
                    if (!isUpdatingFromCode) {
                        try {
                            val quantity = s.toString().toIntOrNull() ?: 1
                            val currentQuantity = etQuantity.text.toString().toIntOrNull() ?: 1
                            if (quantity != currentQuantity) {
                                updateQuantity(quantity)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })

            // Close button
            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            // Cancel button
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            // Borrow button
            btnBorrow.setOnClickListener {
                val quantityText = etQuantity.text.toString()
                if (quantityText.isNotEmpty()) {
                    try {
                        val requestedQuantity = quantityText.toInt()
                        when {
                            requestedQuantity <= 0 -> {
                                Toast.makeText(context, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                            }
                            requestedQuantity > item.availableStock -> {
                                Toast.makeText(context, "Jumlah melebihi stok yang tersedia (${item.availableStock})", Toast.LENGTH_LONG).show()
                            }
                            item.availableStock <= 0 -> {
                                Toast.makeText(context, "Barang kosong - ${item.name} tidak dapat dipinjam", Toast.LENGTH_LONG).show()
                                dialog.dismiss()
                            }
                            else -> {
                                // Show loading
                                btnBorrow.isEnabled = false
                                btnBorrow.text = "Memproses..."

                                viewModel.submitBorrowRequest(item.id, userId, requestedQuantity)
                                Toast.makeText(context, "Pengajuan peminjaman $requestedQuantity ${item.name} berhasil diajukan", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()

                                // Refresh data untuk update stok
                                viewModel.loadItems()
                            }
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Masukkan jumlah yang valid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Masukkan jumlah yang ingin dipinjam", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error membuka dialog peminjaman", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeData() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            binding.swipeRefresh.isRefreshing = false

            // Filter to show only available items for students
            val availableItems = items.filter { it.availableStock > 0 }
            adapter.submitList(availableItems)
            updateEmptyState(availableItems.isEmpty())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!binding.swipeRefresh.isRefreshing) {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            binding.swipeRefresh.isRefreshing = false
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        val emptyVisibility = if (isEmpty) View.VISIBLE else View.GONE
        val recyclerVisibility = if (isEmpty) View.GONE else View.VISIBLE

        binding.tvEmpty.visibility = emptyVisibility
        binding.recyclerView.visibility = recyclerVisibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
