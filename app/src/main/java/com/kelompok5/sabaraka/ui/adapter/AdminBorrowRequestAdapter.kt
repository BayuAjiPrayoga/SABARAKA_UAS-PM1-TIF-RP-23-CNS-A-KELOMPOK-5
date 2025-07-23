package com.kelompok5.sabaraka.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelompok5.sabaraka.databinding.ItemBorrowRequestBinding
import com.kelompok5.sabaraka.model.BorrowRequest
import java.text.SimpleDateFormat
import java.util.Locale

class AdminBorrowRequestAdapter(
    private val onApproveClick: (BorrowRequest) -> Unit,
    private val onRejectClick: (BorrowRequest) -> Unit,
    private val onReturnClick: (BorrowRequest) -> Unit
) : ListAdapter<BorrowRequest, AdminBorrowRequestAdapter.BorrowRequestViewHolder>(BorrowRequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowRequestViewHolder {
        val binding = ItemBorrowRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BorrowRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BorrowRequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BorrowRequestViewHolder(private val binding: ItemBorrowRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: BorrowRequest) {
            Log.d("AdminBorrowRequestAdapter", "Binding request: userId=${request.userId}, userName=${request.userName}, userNim=${request.userNim}")
            binding.apply {
                tvUserName.text = if (request.userName.isNotEmpty()) request.userName else "Nama tidak tersedia"
                tvUserNim.text = if (request.userNim.isNotEmpty()) "NIM: ${request.userNim}" else "NIM: -"
                tvUserEmail.text = if (request.userEmail.isNotEmpty()) request.userEmail else "Email tidak tersedia"

                tvItemName.text = if (request.itemName.isNotEmpty()) request.itemName else "Barang tidak tersedia"
                tvQuantity.text = String.format("x %d", request.quantity)

                val statusText = when (request.status) {
                    "pending" -> "MENUNGGU"
                    "approved" -> "DISETUJUI"
                    "rejected" -> "DITOLAK"
                    "returned" -> "DIKEMBALIKAN"
                    else -> request.status.uppercase()
                }
                tvStatus.text = statusText

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvRequestDate.text = request.requestDate?.let { dateFormat.format(it) } ?: "N/A"

                if (request.returnDate != null) {
                    tvReturnDate.text = String.format("Dikembalikan: %s", dateFormat.format(request.returnDate))
                    tvReturnDate.visibility = android.view.View.VISIBLE
                } else {
                    tvReturnDate.visibility = android.view.View.GONE
                }

                val (backgroundColor, textColor) = when (request.status) {
                    "pending" -> Pair("#FF9800", "#FFFFFF")
                    "approved" -> Pair("#4CAF50", "#FFFFFF")
                    "rejected" -> Pair("#F44336", "#FFFFFF")
                    "returned" -> Pair("#2196F3", "#FFFFFF")
                    else -> Pair("#9E9E9E", "#FFFFFF")
                }

                tvStatus.setBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
                tvStatus.setTextColor(android.graphics.Color.parseColor(textColor))

                when (request.status) {
                    "pending" -> {
                        btnApprove.visibility = android.view.View.VISIBLE
                        btnReject.visibility = android.view.View.VISIBLE
                        btnReturn.visibility = android.view.View.GONE

                        btnApprove.setOnClickListener { onApproveClick(request) }
                        btnReject.setOnClickListener { onRejectClick(request) }
                    }
                    "approved" -> {
                        btnApprove.visibility = android.view.View.GONE
                        btnReject.visibility = android.view.View.GONE
                        btnReturn.visibility = android.view.View.VISIBLE

                        btnReturn.setOnClickListener { onReturnClick(request) }
                    }
                    "rejected", "returned" -> {
                        btnApprove.visibility = android.view.View.GONE
                        btnReject.visibility = android.view.View.GONE
                        btnReturn.visibility = android.view.View.GONE
                    }
                    else -> {
                        btnApprove.visibility = android.view.View.GONE
                        btnReject.visibility = android.view.View.GONE
                        btnReturn.visibility = android.view.View.GONE
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