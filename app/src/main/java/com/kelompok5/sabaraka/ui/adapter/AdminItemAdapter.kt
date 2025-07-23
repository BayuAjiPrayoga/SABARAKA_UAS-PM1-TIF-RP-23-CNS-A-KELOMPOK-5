package com.kelompok5.sabaraka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok5.sabaraka.databinding.ItemAdminItemBinding
import com.kelompok5.sabaraka.model.Item
import com.google.firebase.Timestamp
import java.util.Date
import android.util.Log

class AdminItemAdapter(
    private val onEditClick: (Item) -> Unit,
    private val onDeleteClick: (Item) -> Unit
) : ListAdapter<Item, AdminItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemAdminItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ItemAdminItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnEdit.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(item: Item) {
            binding.apply {
                tvItemName.text = item.name
                tvItemDescription.text = item.description
                tvCategory.text = item.category ?: "Tanpa Kategori"
                tvLocation.text = item.location ?: "Lokasi tidak diset"

                val stockText = "Stok: ${item.availableStock}/${item.stock}"
                tvItemStock.text = stockText

                Log.d("AdminItemAdapter", "Binding item: ${item.name}, stock: ${item.stock}, availableStock: ${item.availableStock}")

                val (statusText, statusColor) = when {
                    item.availableStock == 0 -> {
                        Log.d("AdminItemAdapter", "Status: Kosong for item ${item.name}, availableStock: ${item.availableStock}")
                        "Kosong" to "#F44336"
                    }
                    item.availableStock < item.stock -> "Sebagian Dipinjam" to "#FF9800"
                    else -> "Tersedia" to "#4CAF50"
                }

                tvItemStatus.apply {
                    text = statusText
                    setTextColor(android.graphics.Color.parseColor(statusColor))
                }

                if (!item.imageUrl.isNullOrEmpty()) {
                    com.bumptech.glide.Glide.with(itemView.context)
                        .load(item.imageUrl)
                        .placeholder(com.kelompok5.sabaraka.R.drawable.placeholder_image)
                        .error(com.kelompok5.sabaraka.R.drawable.error_image)
                        .into(imgItem)
                } else {
                    imgItem.setImageResource(com.kelompok5.sabaraka.R.drawable.placeholder_image)
                }

                val dateToFormat = item.updatedAt?.toDate() ?: Date(System.currentTimeMillis())
                val formattedDate = android.text.format.DateFormat.format(
                    "dd MMM yyyy HH:mm",
                    dateToFormat
                )
                tvLastUpdated.text = "Diperbarui: $formattedDate"
            }
        }
    }

    private class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}