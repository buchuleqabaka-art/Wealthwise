package com.example.wealthwise.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.wealthwise.database.entities.Category
import com.example.wealthwise.databinding.FragmentAddCategoryBinding
import com.example.wealthwise.viewmodel.TransactionViewModel

class AddCategoryFragment : Fragment() {

    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    // Default color since the picker UI is missing in the layout
    private var selectedColor: String = "#2B5CE6"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            updateToggleUI(checkedId)
        }

        binding.btnSaveCategory.setOnClickListener {
            saveCategory()
        }

        // Initialize UI
        updateToggleUI(binding.rbExpense.id)
    }

    private fun updateToggleUI(checkedId: Int) {
        if (checkedId == binding.rbExpense.id) {
            binding.rbExpense.setBackgroundResource(com.example.wealthwise.R.drawable.btn_blue_pill)
            binding.rbIncome.setBackgroundResource(com.example.wealthwise.R.drawable.btn_blue_pill_transparent)
        } else {
            binding.rbIncome.setBackgroundResource(com.example.wealthwise.R.drawable.btn_blue_pill)
            binding.rbExpense.setBackgroundResource(com.example.wealthwise.R.drawable.btn_blue_pill_transparent)
        }
    }

    private fun saveCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        val limitText = binding.etBudgetLimit.text.toString().trim()
        val limit = limitText.toDoubleOrNull() ?: 0.0
        val type = if (binding.rbExpense.isChecked) "EXPENSE" else "INCOME"

        if (name.isEmpty()) {
            binding.etCategoryName.error = "Please enter a category name"
            binding.etCategoryName.requestFocus()
            return
        }

        val category = Category(
            name = name,
            type = type,
            budgetLimit = limit,
            color = selectedColor
        )

        viewModel.insertCategory(category)

        Toast.makeText(requireContext(), "'$name' category saved!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}
