package com.kelompok5.sabaraka.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.kelompok5.sabaraka.databinding.FragmentAddItemBinding
import com.kelompok5.sabaraka.model.Item
import com.kelompok5.sabaraka.ui.ItemViewModel

class AddItemFragment : Fragment() {
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ItemViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(requireActivity())[ItemViewModel::class.java]

        setupUI()
        setupCategorySpinner()
        observeData()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            saveItem()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            "Elektronik",
            "Alat Tulis",
            "Buku",
            "Peralatan Lab",
            "Olahraga",
            "Musik",
            "Lainnya"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun saveItem() {
        val name = binding.etItemName.text.toString().trim()
        val description = binding.etItemDescription.text.toString().trim()
        val stockText = binding.etItemStock.text.toString().trim()
        val category = binding.spinnerCategory.text.toString().trim()

        if (!validateInput(name, description, category, stockText)) {
            return
        }

        val stock = stockText.toInt()
        val currentUserId = auth.currentUser?.uid ?: ""

        val item = Item() // Gunakan konstruktor default
        item.name = name
        item.description = description
        item.category = category
        item.status = "available"
        item.stock = stock
        item.availableStock = stock
        item.quantity = stock
        item.createdBy = currentUserId
        item.createdAt = Timestamp.now() // Gunakan Timestamp
        item.updatedAt = Timestamp.now() // Gunakan Timestamp

        viewModel.addItem(item)
    }

    private fun validateInput(name: String, description: String, category: String, stockText: String): Boolean {
        if (name.isEmpty()) {
            binding.etItemName.error = "Nama barang harus diisi"
            binding.etItemName.requestFocus()
            return false
        }

        if (description.isEmpty()) {
            binding.etItemDescription.error = "Deskripsi barang harus diisi"
            binding.etItemDescription.requestFocus()
            return false
        }

        if (category.isEmpty()) {
            binding.spinnerCategory.error = "Kategori barang harus dipilih"
            binding.spinnerCategory.requestFocus()
            return false
        }

        if (stockText.isEmpty()) {
            binding.etItemStock.error = "Stok barang harus diisi"
            binding.etItemStock.requestFocus()
            return false
        }

        val stock = stockText.toIntOrNull()
        if (stock == null || stock <= 0) {
            binding.etItemStock.error = "Stok barang harus berupa angka positif"
            binding.etItemStock.requestFocus()
            return false
        }

        return true
    }

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
            binding.btnCancel.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Barang berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                clearForm()
                findNavController().navigateUp()
            }
        }
    }

    private fun clearForm() {
        binding.etItemName.setText("")
        binding.etItemDescription.setText("")
        binding.etItemStock.setText("")
        binding.spinnerCategory.setText("")

        binding.etItemName.error = null
        binding.etItemDescription.error = null
        binding.etItemStock.error = null
        binding.spinnerCategory.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}