package com.kelompok5.sabaraka.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok5.sabaraka.R
import com.kelompok5.sabaraka.databinding.ItemBorrowHistoryBinding
import com.kelompok5.sabaraka.model.BorrowRequest
import java.text.SimpleDateFormat
import java.util.Locale

class BorrowHistoryAdapter : ListAdapter<BorrowRequest, BorrowHistoryAdapter.BorrowHistoryViewHolder>(BorrowRequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowHistoryViewHolder {
        val binding = ItemBorrowHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BorrowHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BorrowHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BorrowHistoryViewHolder(private val binding: ItemBorrowHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: BorrowRequest) {
            binding.apply {
                // Tampilkan nama item jika tersedia, jika tidak tampilkan ID
                tvItemName.text = if (request.itemName.isNotEmpty()) {
                    request.itemName
                } else {
                    "Barang ID: ${request.itemId}"
                }

                // Tampilkan kuantitas
                tvQuantity.text = "Jumlah: ${request.quantity}"

                // Set status dengan warna dan teks yang sesuai
                when (request.status) {
                    "pending" -> {
                        tvStatus.text = "Menunggu Persetujuan"
                        tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                        tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_pending))
                    }
                    "approved" -> {
                        tvStatus.text = "Disetujui"
                        tvStatus.setBackgroundResource(R.drawable.bg_status_approved)
                        tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_approved))
                    }
                    "rejected" -> {
                        tvStatus.text = "Ditolak"
                        tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                        tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_rejected))
                    }
                    "returned" -> {
                        tvStatus.text = "Dikembalikan"
                        tvStatus.setBackgroundResource(R.drawable.bg_status_returned)
                        tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_returned))
                    }
                    else -> {
                        tvStatus.text = request.status.replaceFirstChar { it.uppercase() }
                        tvStatus.setBackgroundResource(R.drawable.bg_status_default)
                        tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                    }
                }

                // Format dan tampilkan tanggal request
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                tvRequestDate.text = "Tanggal Pengajuan: ${dateFormat.format(request.requestDate)}"

                // Tampilkan tanggal return jika ada
                if (request.status == "returned" && request.returnDate != null) {
                    tvReturnDate.text = "Tanggal Kembali: ${dateFormat.format(request.returnDate!!)}"
                    tvReturnDate.visibility = View.VISIBLE
                } else {
                    tvReturnDate.visibility = View.GONE
                }

                // Tampilkan keterangan tambahan berdasarkan status
                when (request.status) {
                    "pending" -> {
                        tvDescription.text = "Pengajuan Anda sedang menunggu persetujuan dari admin"
                        tvDescription.visibility = View.VISIBLE
                    }
                    "approved" -> {
                        tvDescription.text = "Pengajuan Anda telah disetujui. Silahkan ambil barang di tempat yang telah ditentukan"
                        tvDescription.visibility = View.VISIBLE
                    }
                    "rejected" -> {
                        tvDescription.text = "Pengajuan Anda ditolak. Silahkan hubungi admin untuk informasi lebih lanjut"
                        tvDescription.visibility = View.VISIBLE
                    }
                    "returned" -> {
                        tvDescription.text = "Barang telah berhasil dikembalikan"
                        tvDescription.visibility = View.VISIBLE
                    }
                    else -> {
                        tvDescription.visibility = View.GONE
                    }
                }
            }
        }
    }

    class BorrowRequestDiffCallback : DiffUtil.ItemCallback<BorrowRequest>() {
        override fun areItemsTheSame(oldItem: BorrowRequest, newItem: BorrowRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BorrowRequest, newItem: BorrowRequest): Boolean {
            return oldItem == newItem
        }
    }
}
