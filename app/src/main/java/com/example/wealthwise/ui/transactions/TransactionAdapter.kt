package com.example.wealthwise.ui.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wealthwise.R
import com.example.wealthwise.database.entities.TransactionWithCategory
import com.example.wealthwise.databinding.ItemTransactionBinding

class TransactionAdapter : ListAdapter<TransactionWithCategory, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionWithCategory) {
            val transaction = item.transaction
            val category = item.category
            
            binding.tvDescription.text = transaction.description.ifEmpty { category?.name ?: "No Description" }
            binding.tvDate.text = transaction.date
            
            val prefix = if (transaction.type == "EXPENSE") "- R" else "+ R"
            binding.tvAmount.text = "$prefix %.2f".format(transaction.amount)
            
            val amountColor = if (transaction.type == "EXPENSE") "#E24B4A".toColorInt() else "#1D9E75".toColorInt()
            binding.tvAmount.setTextColor(amountColor)

            // Set Category Icon and Color
            val catColorStr = category?.color ?: "#7BA7D4"
            try {
                binding.ivCategoryIcon.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(catColorStr))
            } catch (e: Exception) {
                binding.ivCategoryIcon.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#7BA7D4"))
            }
            
            // Set icon resource if available, else default
            binding.ivCategoryIcon.setImageResource(R.drawable.ic_category_default)
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionWithCategory>() {
        override fun areItemsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean {
            return oldItem.transaction.id == newItem.transaction.id
        }

        override fun areContentsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean {
            return oldItem == newItem
        }
    }
}
