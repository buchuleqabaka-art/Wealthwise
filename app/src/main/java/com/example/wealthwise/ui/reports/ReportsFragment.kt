package com.example.wealthwise.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.wealthwise.databinding.FragmentReportsBinding
import com.example.wealthwise.viewmodel.TransactionViewModel
import com.github.mikephil.charting.data.*
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupCharts()
        observeData()
    }

    private fun setupCharts() {
        // Setup Pie Chart
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 45f
            setHoleColor(Color.TRANSPARENT)
            legend.isEnabled = false
            setEntryLabelColor(Color.WHITE)
            animateY(800)
        }

        // Setup Line Chart
        binding.lineChart.apply {
            description.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            animateX(800)
        }
    }

    private fun observeData() {
        // Pie Chart Data
        viewModel.categoryTotalsInfo.observe(viewLifecycleOwner) { totals ->
            if (!totals.isNullOrEmpty()) {
                binding.pieChart.visibility = View.VISIBLE
                updatePieChart(totals)
                updateTopSpendingList(totals)
            } else {
                binding.pieChart.visibility = View.GONE
                clearTopSpendingList()
            }
        }

        // Line Chart Data (Last 30 days)
        viewModel.monthlyDailySpending.observe(viewLifecycleOwner) { dailySums ->
            if (!dailySums.isNullOrEmpty()) {
                binding.lineChart.visibility = View.VISIBLE
                updateLineChart(dailySums.map { it.total })
            } else {
                binding.lineChart.visibility = View.GONE
            }
        }
    }

    private fun updatePieChart(totals: List<com.example.wealthwise.viewmodel.CategoryTotal>) {
        val entries = totals.map { PieEntry(it.amount.toFloat(), it.name) }
        val colors = totals.map { 
            try { Color.parseColor(it.color) } catch (e: Exception) { Color.GRAY }
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.WHITE
            valueTextSize = 10f
            setDrawValues(false)
        }

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun updateLineChart(values: List<Double>) {
        val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) }
        val dataSet = LineDataSet(entries, "Spending").apply {
            color = "#00E5FF".toColorInt()
            setCircleColor("#00E5FF".toColorInt())
            lineWidth = 2f
            circleRadius = 3f
            setDrawFilled(true)
            fillColor = "#00E5FF".toColorInt()
            fillAlpha = 50
            valueTextColor = Color.WHITE
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
    }

    private fun clearTopSpendingList() {
        binding.tvFood.text = "R 0"
        binding.tvTransport.text = "R 0"
        binding.tvEntertainment.text = "R 0"
        
        // Reset labels to default if needed, or hide them
        (binding.llTopSpending.getChildAt(0) as? ViewGroup)?.getChildAt(0)?.let { (it as? android.widget.TextView)?.text = "• Food" }
        (binding.llTopSpending.getChildAt(2) as? ViewGroup)?.getChildAt(0)?.let { (it as? android.widget.TextView)?.text = "• Transport" }
        (binding.llTopSpending.getChildAt(4) as? ViewGroup)?.getChildAt(0)?.let { (it as? android.widget.TextView)?.text = "• Entertainment" }
    }

    private fun updateTopSpendingList(totals: List<com.example.wealthwise.viewmodel.CategoryTotal>) {
        val sorted = totals.sortedByDescending { it.amount }
        
        clearTopSpendingList() // Start fresh

        // Update first row
        if (sorted.size > 0) {
            val item = sorted[0]
            (binding.llTopSpending.getChildAt(0) as? ViewGroup)?.let { view ->
                (view.getChildAt(0) as? android.widget.TextView)?.text = "• ${item.name}"
                binding.tvFood.text = "R ${item.amount.toInt()}"
            }
        }

        // Update second row
        if (sorted.size > 1) {
            val item = sorted[1]
            (binding.llTopSpending.getChildAt(2) as? ViewGroup)?.let { view ->
                (view.getChildAt(0) as? android.widget.TextView)?.text = "• ${item.name}"
                binding.tvTransport.text = "R ${item.amount.toInt()}"
            }
        }

        // Update third row
        if (sorted.size > 2) {
            val item = sorted[2]
            (binding.llTopSpending.getChildAt(4) as? ViewGroup)?.let { view ->
                (view.getChildAt(0) as? android.widget.TextView)?.text = "• ${item.name}"
                binding.tvEntertainment.text = "R ${item.amount.toInt()}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
