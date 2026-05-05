package com.example.wealthwise.ui.budget

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wealthwise.R
import com.example.wealthwise.databinding.FragmentBudgetBinding
import com.example.wealthwise.viewmodel.TransactionViewModel

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnAddCategory.setOnClickListener {
            findNavController().navigate(R.id.action_budgetFragment_to_addCategoryFragment)
        }

        binding.tvHeading.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter(
            onEditClick = { category ->
                // TODO: Navigate to Edit Category screen
                Toast.makeText(requireContext(), "Edit: ${category.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { category ->
                viewModel.deleteCategory(category)
                Toast.makeText(requireContext(), "Deleted: ${category.name}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvBudgetDetails.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.budgetData.observe(viewLifecycleOwner) { data ->
            if (data.isNullOrEmpty()) {
                binding.rvBudgetDetails.visibility = View.GONE
                binding.tvNoCategoriesMsg.visibility = View.VISIBLE
                
                binding.tvTotalBudget.text = "R 0"
                binding.tvTotalSpent.text = "R 0"
                binding.tvRemaining.text = "R 0"
                binding.tvProgressPercent.text = "0%"
                binding.progressOverall.progress = 0
            } else {
                binding.rvBudgetDetails.visibility = View.VISIBLE
                binding.tvNoCategoriesMsg.visibility = View.GONE
                
                budgetAdapter.submitList(data)
                
                val totalBudget = data.sumOf { it.category.budgetLimit }
                val totalSpent = data.sumOf { it.spent }
                
                binding.tvTotalBudget.text = "R %.0f".format(totalBudget)
                binding.tvTotalSpent.text = "R %.0f".format(totalSpent)
                binding.tvRemaining.text = "R %.0f".format(totalBudget - totalSpent)

                val overallPct = if (totalBudget > 0)
                    ((totalSpent / totalBudget) * 100).toInt().coerceIn(0, 100) else 0
                binding.tvProgressPercent.text = "$overallPct%"
                binding.progressOverall.progress = overallPct
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
