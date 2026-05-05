package com.example.wealthwise.ui.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wealthwise.database.entities.Category
import com.example.wealthwise.databinding.ItemBudgetCategoryBinding

data class BudgetCategory(
    val category: Category,
    val spent: Double
)

class BudgetAdapter(
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<BudgetCategory, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BudgetViewHolder(private val binding: ItemBudgetCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BudgetCategory) {
            val category = item.category
            val spent = item.spent
            val limit = category.budgetLimit

            binding.tvCategoryName.text = category.name
            binding.tvSpent.text = "R %.0f".format(spent)
            binding.tvBudget.text = "of R %.0f".format(limit)

            val pct = if (limit > 0) ((spent / limit) * 100).toInt().coerceIn(0, 100) else 0
            binding.progressCategory.progress = pct
            binding.tvPercent.text = "$pct% used"

            // Alert for near limit
            if (pct >= 80) {
                binding.tvLimitAlert.visibility = View.VISIBLE
                binding.tvLimitAlert.text = "⚠️ $pct% of budget used"
            } else {
                binding.tvLimitAlert.visibility = View.GONE
            }

            // Status badge
            binding.tvStatusBadge.visibility = View.VISIBLE
            if (spent > limit && limit > 0) {
                binding.tvStatusBadge.text = "Over Budget"
                binding.tvStatusBadge.setTextColor(android.graphics.Color.RED)
            } else {
                binding.tvStatusBadge.text = "On Track"
                binding.tvStatusBadge.setTextColor(android.graphics.Color.CYAN)
            }

            binding.ivEditCategory.setOnClickListener { onEditClick(category) }
            binding.ivDeleteCategory.setOnClickListener { onDeleteClick(category) }
        }
    }

    class BudgetDiffCallback : DiffUtil.ItemCallback<BudgetCategory>() {
        override fun areItemsTheSame(oldItem: BudgetCategory, newItem: BudgetCategory): Boolean {
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: BudgetCategory, newItem: BudgetCategory): Boolean {
            return oldItem == newItem
        }
    }
}
