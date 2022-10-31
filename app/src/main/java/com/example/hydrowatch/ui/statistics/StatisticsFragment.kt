package com.example.hydrowatch.ui.statistics

import android.R.attr.fillColor
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hydrowatch.DatabaseContract
import com.example.hydrowatch.DatabaseManager
import com.example.hydrowatch.MainActivity
import com.example.hydrowatch.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.DecimalFormat
import java.util.Calendar


class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var textUsage: TextView? = null
    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
    private fun generateDataLines(date: Calendar): Triple<LineData, Double, Double> {
        val valuesFlow: ArrayList<Entry> = ArrayList()
        val valuesEnergy: ArrayList<Entry> = ArrayList()
        var totalFlow: Double = 0.0
        var totalEnergy: Double = 0.0

        for (i in 0..23) {
            var flow: Double
            var energy: Double

            date.set(Calendar.HOUR_OF_DAY, i)
            val cursor = DatabaseManager.getInstance(null).readFromDB(DatabaseManager.dateFormat.format(date.time))
            if(cursor.count >= 2) {
                throw Error("Multiple records for the same hour in database")
            } else if (cursor.count == 0) {
                flow = 0.0
                energy = 0.0
            } else {
                cursor.moveToFirst()
                flow = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_FLOW))
                energy = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Usage.COLUMN_ENERGY))
            }
            valuesFlow.add(
                Entry(
                    i.toFloat(),
                    flow.toFloat()
                )
            )
            valuesEnergy.add(Entry(i.toFloat(), (energy/1000).toFloat()))
            totalFlow += flow
            totalEnergy += energy
        }
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
        return Triple(LineData(sets), totalFlow, totalEnergy)
    }

    private fun chartSetup(chart:LineChart) {
        chart.setBackgroundColor(Color.WHITE)
        chart.setGridBackgroundColor(fillColor)
        chart.setDrawGridBackground(true)

        chart.setDrawBorders(true)

        chart.description.isEnabled = false


        chart.setTouchEnabled(false)

        val l = chart.legend
        l.isEnabled = true
        l.xEntrySpace = 150F

        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        xAxis.axisMaximum = 24f

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
    private fun chartRefresh(chart:LineChart, time:Calendar) {
        val (lineData, totalFlow, totalEnergy) = generateDataLines(time)
        chart.data = lineData
        chart.invalidate() // refresh
        val df = DecimalFormat("#.#")
        textUsage?.text = "On this day, you used ${df.format(totalFlow)} L of water and ${df.format(totalEnergy/1000)} kWh of energy"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val usageViewModel =
            ViewModelProvider(this)[UsageViewModel::class.java]

        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textUsage
        usageViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val chart = binding.chart
        val calendar = binding.calendarView
        textUsage = binding.textUsage
        chartSetup(chart)
        chartRefresh(chart, Calendar.getInstance())

//        var currentCalendar: Calendar = Calendar.getInstance()
//        calendar.set(year, month, date)
//            Log.d("CalendarChange", DatabaseManager.dateFormat.format(calendar.time))
//            chartRefresh(chart, calendar)


        calendar.setOnDateChangeListener { _, year, month, date ->
            val calendarTime: Calendar = Calendar.getInstance()
            calendarTime.set(year, month, date)
            Log.d("CalendarChange", DatabaseManager.dateFormat.format(calendarTime.time))
            chartRefresh(chart, calendarTime)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarTitle("Statistics")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}