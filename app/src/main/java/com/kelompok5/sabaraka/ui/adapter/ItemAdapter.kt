package com.kelompok5.sabaraka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok5.sabaraka.databinding.ItemItemBinding
import com.kelompok5.sabaraka.model.Item

class ItemAdapter(
    private val onItemClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.apply {
                tvItemName.text = item.name
                tvItemDescription.text = item.description

                // Format stok dengan lebih informatif
                tvItemStock.text = "Stok Tersedia: ${item.availableStock}/${item.stock}"

                // Set status dan indikator berdasarkan availableStock
                when {
                    item.availableStock > 0 -> {
                        tvItemStatus.text = "Tersedia"
                        tvItemStatus.setBackgroundResource(com.kelompok5.sabaraka.R.drawable.status_badge)
                        tvItemStatus.setTextColor(itemView.context.getColor(android.R.color.white))

                        // Indikator hijau untuk stok tersedia
                        tvStockIndicator.text = "●"
                        tvStockIndicator.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))

                        btnBorrow.isEnabled = true
                        btnBorrow.text = "PINJAM BARANG"
                        btnBorrow.backgroundTintList = itemView.context.getColorStateList(android.R.color.holo_blue_bright)
                    }
                    else -> {
                        tvItemStatus.text = "Habis"
                        tvItemStatus.setBackgroundResource(com.kelompok5.sabaraka.R.drawable.status_badge_red)
                        tvItemStatus.setTextColor(itemView.context.getColor(android.R.color.white))

                        // Indikator merah untuk stok habis
                        tvStockIndicator.text = "●"
                        tvStockIndicator.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))

                        btnBorrow.isEnabled = false
                        btnBorrow.text = "STOK HABIS"
                        btnBorrow.backgroundTintList = itemView.context.getColorStateList(android.R.color.darker_gray)
                    }
                }

                // Set click listener hanya jika barang tersedia
                if (item.availableStock > 0) {
                    btnBorrow.setOnClickListener { onItemClick(item) }
                    root.alpha = 1.0f
                } else {
                    btnBorrow.setOnClickListener(null)
                    root.alpha = 0.7f // Membuat card terlihat sedikit pudar jika habis
                }
            }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}
