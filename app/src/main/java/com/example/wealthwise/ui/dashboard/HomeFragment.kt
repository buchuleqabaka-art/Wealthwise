package com.example.wealthwise.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.wealthwise.R
import com.example.wealthwise.SessionManager
import com.example.wealthwise.databinding.FragmentHomeBinding
import com.example.wealthwise.viewmodel.TransactionViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = SessionManager.getUserName(requireContext())
        binding.tvGreeting.text = getString(R.string.greeting, userName)

        setupCharts()
        observeViewModel()

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }
        
        binding.ivMenu.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    private fun setupCharts() {
        // Configure Pie Chart
        binding.pieChart.apply {
            description.isEnabled = false
            legend.apply { isEnabled = true; textColor = Color.GRAY; textSize = 8f }
            isDrawHoleEnabled = true
            holeRadius = 32f
            setHoleColor(Color.TRANSPARENT)
            setNoDataText("")
        }

        // Configure Line Chart
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.GRAY; textSize = 8f
                setDrawGridLines(false)
            }
            axisLeft.apply { textColor = Color.GRAY; axisMinimum = 0f }
            axisRight.isEnabled = false
            setNoDataText("")
        }
    }

    private fun observeViewModel() {
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.tvIncome.text = "+ R %.2f".format(income ?: 0.0)
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
            binding.tvExpenses.text = "- R %.2f".format(expenses ?: 0.0)
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = "R %.2f".format(balance ?: 0.0)
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isNullOrEmpty()) {
                binding.pieChart.visibility = View.GONE
                binding.lineChart.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            } else {
                binding.tvNoData.visibility = View.GONE
            }
        }

        viewModel.categoryTotalsInfo.observe(viewLifecycleOwner) { categoryTotals ->
            if (!categoryTotals.isNullOrEmpty()) {
                updatePieChart(categoryTotals)
            } else {
                binding.pieChart.visibility = View.GONE
            }
        }

        viewModel.monthlyDailySpending.observe(viewLifecycleOwner) { dailySpending ->
            if (!dailySpending.isNullOrEmpty()) {
                binding.lineChart.visibility = View.VISIBLE
                updateLineChart(dailySpending)
            } else {
                binding.lineChart.visibility = View.GONE
            }
        }
    }

    private fun updatePieChart(categoryTotals: List<com.example.wealthwise.viewmodel.CategoryTotal>) {
        binding.pieChart.visibility = View.VISIBLE
        
        val pieEntries = categoryTotals.map { PieEntry(it.amount.toFloat(), it.name) }
        val colors = categoryTotals.map { 
            try { it.color.toColorInt() } catch (_: Exception) { Color.GRAY }
        }

        val ds = PieDataSet(pieEntries, "").apply {
            this.colors = colors
            setDrawValues(false)
            sliceSpace = 2f
        }
        
        binding.pieChart.data = PieData(ds)
        binding.pieChart.animateY(800)
        binding.pieChart.invalidate()
    }

    private fun updateLineChart(dailySpending: List<com.example.wealthwise.database.entities.DateSum>) {
        val entries = dailySpending.mapIndexed { index, dateSum -> 
            Entry(index.toFloat(), dateSum.total.toFloat()) 
        }
        
        val dates = dailySpending.map { it.date.takeLast(5) } // Show MM-DD

        val ds = LineDataSet(entries, "Spending").apply {
            color = "#2B5CE6".toColorInt()
            setCircleColor("#2B5CE6".toColorInt())
            lineWidth = 2f; circleRadius = 3f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = "#2B5CE6".toColorInt()
            fillAlpha = 30
        }

        binding.lineChart.apply {
            data = LineData(ds)
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            animateX(700)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
