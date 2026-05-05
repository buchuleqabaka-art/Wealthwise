package com.example.wealthwise.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wealthwise.R
import com.example.wealthwise.databinding.FragmentTransactionListBinding
import com.example.wealthwise.viewmodel.TransactionViewModel

class TransactionListFragment : Fragment() {

    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        android.util.Log.d("TransactionListFragment", "onCreateView started")
        _binding = FragmentTransactionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_transactionListFragment_to_addTransactionFragment)
        }

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun observeData() {
        viewModel.allTransactionsWithCategory.observe(viewLifecycleOwner) { transactions ->
            if (transactions == null || transactions.isEmpty()) {
                binding.rvTransactions.visibility = View.GONE
                binding.tvEmptyMessage.visibility = View.VISIBLE
            } else {
                binding.rvTransactions.visibility = View.VISIBLE
                binding.tvEmptyMessage.visibility = View.GONE
                adapter.submitList(transactions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
