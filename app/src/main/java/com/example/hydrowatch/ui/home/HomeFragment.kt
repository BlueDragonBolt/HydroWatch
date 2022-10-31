package com.example.hydrowatch.ui.home

import android.R
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hydrowatch.DatabaseContract
import com.example.hydrowatch.DatabaseManager
import com.example.hydrowatch.MainActivity
import com.example.hydrowatch.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    data class WeeklyChartData (val lineData: LineData, val dailyFlow: Double, val dailyEnergy: Double, val weeklyFlow: Double, val weeklyEnergy: Double){
    }
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var lastWeeklyChartData: WeeklyChartData? = null

    companion object {
        private var instance: HomeFragment? = null
        fun getInstance(): HomeFragment? {
            return instance
        }
    }


    class DateAxisFormatter : ValueFormatter() {
        @SuppressLint("SimpleDateFormat")
        private val dateFormat: DateFormat = SimpleDateFormat("dd/MM")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val date = Calendar.getInstance()
            date.add(Calendar.DATE, -(6-value.toInt()))
            return dateFormat.format(date.time)
        }
    }

    private fun generateDataLines(): WeeklyChartData {
        val valuesFlow: ArrayList<Entry> = ArrayList()
        val valuesEnergy: ArrayList<Entry> = ArrayList()
        var totalFlow = 0.0
        var totalEnergy = 0.0
        var dailyFlow = 0.0
        var dailyEnergy = 0.0
        val date = Calendar.getInstance()
        Log.d("generateDataLines", "today: " + DatabaseManager.dateFormat.format(date.time))
        date.set(Calendar.DATE, date.get(Calendar.DATE) - 6)
        for (i in 0 .. 6) {
            dailyFlow = 0.0
            dailyEnergy = 0.0
            for (j in 0..23) {
                date.set(Calendar.HOUR_OF_DAY, j)
                val cursor = DatabaseManager.getInstance(null)
                    .readFromDB(DatabaseManager.dateFormat.format(date.time))
                Log.d("generateDataLines", "ask for: " + DatabaseManager.dateFormat.format(date.time))
                if (cursor.count >= 2) {
                    throw Error("Multiple records for the same hour in database")
                } else if (cursor.count != 0) {
                    cursor.moveToFirst()
                    dailyFlow +=
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_FLOW))
                    Log.d("generateDataLines","hourly flow: " +
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_FLOW)).toString())

                    dailyEnergy +=
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_ENERGY))
                }
            }
            valuesFlow.add(
                Entry(
                    i.toFloat(),
                    dailyFlow.toFloat()
                )
            )
            valuesEnergy.add(Entry(i.toFloat(), (dailyEnergy/1000.0).toFloat()))
            totalFlow += dailyFlow
            totalEnergy += dailyEnergy
            date.set(Calendar.DATE, date.get(Calendar.DATE) + 1)
        }
        Log.d("generateDataLines", "totalFlow: $totalFlow")
        val d1 = LineDataSet(valuesFlow, "Water, in L")
        d1.lineWidth = 2.5f
        d1.circleRadius = 4.5f
        d1.highLightColor = Color.rgb(244, 117, 117)
        d1.setDrawValues(false)
        d1.axisDependency = YAxis.AxisDependency.LEFT
        val d2 = LineDataSet(valuesEnergy, "Energy, in kWh")
        d2.lineWidth = 2.5f
        d2.circleRadius = 4.5f
        d2.highLightColor = Color.rgb(244, 117, 117)
        d2.color = ColorTemplate.VORDIPLOM_COLORS[2]
        d2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
        d2.setDrawValues(false)
        d2.axisDependency = YAxis.AxisDependency.RIGHT
        val sets: ArrayList<ILineDataSet> = ArrayList()
        sets.add(d1)
        sets.add(d2)
        lastWeeklyChartData = WeeklyChartData(LineData(sets), dailyFlow, dailyEnergy, totalFlow, totalEnergy)
        return WeeklyChartData(LineData(sets), dailyFlow, dailyEnergy, totalFlow, totalEnergy)
    }

    private fun chartSetup(chart: LineChart) {
        chart.setBackgroundColor(Color.WHITE)
        chart.setGridBackgroundColor(R.attr.fillColor)
        chart.setDrawGridBackground(true)

        chart.setDrawBorders(true)

        chart.description.isEnabled = false


        chart.setTouchEnabled(false)

        val l = chart.legend
        l.isEnabled = true
        l.xEntrySpace = 150F

        val xAxis = chart.xAxis
        xAxis.isEnabled = true
//        xAxis.axisMaximum = 24f
        chart.xAxis.valueFormatter = DateAxisFormatter()
        val leftAxis = chart.axisLeft
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawZeroLine(false)
        leftAxis.setDrawGridLines(false)
//        leftAxis.axisMinimum = -0.4f
        leftAxis.granularity = 0.5f
        leftAxis.isGranularityEnabled = true

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = true
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.setDrawGridLines(false)
//        rightAxis.axisMinimum = -0.4f
        rightAxis.granularity = 0.5f
        rightAxis.isGranularityEnabled = true
    }

    @SuppressLint("SetTextI18n")
    fun updateHomeView(inputData: WeeklyChartData? = lastWeeklyChartData, deltaWater: Double = 0.0, deltaEnergy: Double = 0.0){
        val data = generateDataLines()
        if(_binding == null) {
            Log.d("Update home view", "Binding is null")
        }
        binding.chart.data = data.lineData
        binding.chart.invalidate() // refresh
        val df = DecimalFormat("#.#")
        binding.dailyUsageText.text = "You've used ${df.format(data.dailyFlow)} L of water and ${df.format(data.dailyEnergy/1000)} kWh of energy today."
        binding.weeklyUsageText.text = "Your total weekly consumption is ${df.format(data.weeklyFlow)} L and ${df.format(data.weeklyEnergy/1000)} kWh."
        binding.progressWaterText.text = "${(deltaWater * 1000).toInt()} ml"
        binding.progressEnergyText.text = "${deltaEnergy.toInt()} Wh"
        binding.progressIndicatorWater.progress = if (deltaWater == 0.0) binding.progressIndicatorWater.max else (deltaWater * 1000).toInt()
        binding.progressIndicatorEnergy.progress = if (deltaEnergy == 0.0) binding.progressIndicatorEnergy.max else deltaEnergy.toInt()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        instance = this
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        chartSetup(binding.chart)
        updateHomeView(generateDataLines())

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarTitle("HydroWatch")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        instance = null
    }
}