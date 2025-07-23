package com.kelompok5.sabaraka.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kelompok5.sabaraka.R
import com.kelompok5.sabaraka.databinding.FragmentEditItemBinding
import com.kelompok5.sabaraka.model.Item
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class EditItemFragment : Fragment() {
    private var _binding: FragmentEditItemBinding? = null
    private val binding get() = _binding!!

    private var itemId: String? = null
    private var currentItem: Item? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadItemDataFromFirestore()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateItem.setOnClickListener {
            updateItem()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadItemDataFromFirestore() {
        itemId = arguments?.getString("itemId")
        if (itemId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error: ID barang tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("items").document(itemId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentItem = document.toObject(Item::class.java)?.apply { id =
                        itemId as String
                    }
                    currentItem?.let {
                        binding.etItemName.setText(it.name)
                        binding.etItemDescription.setText(it.description)
                        binding.etItemStock.setText(it.stock.toString())
                        binding.etItemCategory.setText(it.category)
                        if (it.status == "available") {
                            binding.rbAvailable.isChecked = true
                        } else {
                            binding.rbUnavailable.isChecked = true
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Data barang tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateItem() {
        val name = binding.etItemName.text.toString().trim()
        val description = binding.etItemDescription.text.toString().trim()
        val stockText = binding.etItemStock.text.toString().trim()
        val category = binding.etItemCategory.text.toString().trim()
        val status = if (binding.rbAvailable.isChecked) "available" else "unavailable"

        if (name.isEmpty()) {
            binding.etItemName.error = "Nama barang harus diisi"
            return
        }

        if (description.isEmpty()) {
            binding.etItemDescription.error = "Deskripsi barang harus diisi"
            return
        }

        if (stockText.isEmpty()) {
            binding.etItemStock.error = "Stok barang harus diisi"
            return
        }

        val stock = stockText.toIntOrNull()
        if (stock == null || stock < 0) {
            binding.etItemStock.error = "Stok barang harus berupa angka valid"
            return
        }

        if (category.isEmpty()) {
            binding.etItemCategory.error = "Kategori barang harus diisi"
            return
        }

        val itemIdValue = itemId ?: run {
            Toast.makeText(requireContext(), "Error: ID barang tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedItem = currentItem ?: Item()
        updatedItem.id = itemIdValue
        updatedItem.name = name
        updatedItem.description = description
        updatedItem.stock = stock
        updatedItem.availableStock = stock
        updatedItem.category = category
        updatedItem.status = status
        updatedItem.updatedAt = Timestamp.now()

        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpdateItem.isEnabled = false
        binding.btnCancel.isEnabled = false

        db.collection("items").document(itemIdValue)
            .set(updatedItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Barang berhasil diperbaharui!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memperbarui barang: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.progressBar.visibility = View.GONE
                binding.btnUpdateItem.isEnabled = true
                binding.btnCancel.isEnabled = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}