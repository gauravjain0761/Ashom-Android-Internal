package com.ashomapp.presentation.stockDetails

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ashomapp.R
import com.ashomapp.databinding.FragmentFullStockGraphBinding
import com.ashomapp.databinding.GraphFullScreenLayoutBinding
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.presentation.home.stockPrice.model.StockHistoryModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import com.ashomapp.utils.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


class FullStockGraphFragment : Fragment(R.layout.fragment_full_stock_graph), View.OnClickListener {

    private var stockDifference: Double = 0.0
    private var stockPercent: Double = 0.0

    private var maxVolSize: Int = 0
    private var maxLatPrice: Int = 0
    private lateinit var stockData: StockListModelItem
    private lateinit var binding: FragmentFullStockGraphBinding

    private var currentView: View? = null

    private val mHomeViewModel by activityViewModels<HomeViewModel>()

    private lateinit var graphData: StockHistoryModel

    private var oneDaySelected = 1
    private var oneWeekSelected = 0
    private var monthSelected = 0
    private var yearsSelected = 0
    private var spread = "A"
    private var dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var listDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private var dateTimeFormat = SimpleDateFormat("dd MMM yyyy  hh:mm a", Locale.getDefault())
    private var selectedDate = dateFormat.format(Date())

    private var lastPrice: MutableList<Double> = ArrayList()
    private var volumeList: MutableList<Double> = ArrayList()
    private var dateList: MutableList<Date> = ArrayList()

    private var currencySymbol = "SR"

    override fun onCreate(savedInstanceState: Bundle?) {
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_full_stock_graph, container, false)
            binding = FragmentFullStockGraphBinding.bind(currentView!!)
            init()
            setObserver()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {
//        val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("GST")
        val gson = Gson()
        stockData = gson.fromJson(arguments?.getString("stockData"), StockListModelItem::class.java)

        oneDaySelected = arguments?.getInt("oneDaySelected")!!
        oneWeekSelected = arguments?.getInt("oneWeekSelected")!!
        monthSelected = arguments?.getInt("monthSelected")!!
        yearsSelected = arguments?.getInt("yearsSelected")!!
        setFilterSelectionFromPrevious()

        if (stockData.Identifier.contains(".SE")) {
            currencySymbol = "SR"
        } else if (stockData.Identifier.contains(".AD") || stockData.Identifier.contains(".DU")) {
            currencySymbol = "AED"
        } else if (stockData.Identifier.contains(".KW")) {
            currencySymbol = "KD"
        } else if (stockData.Identifier.contains(".BH")) {
            currencySymbol = "BD"
        } else if (stockData.Identifier.contains(".QA")) {
            currencySymbol = "QAR"
        } else if (stockData.Identifier.contains(".OM")) {
            currencySymbol = "OMR"
        }
        setStockData()
    }

    private fun setStockData() {
        binding.close.text = stockData.Universal_Close_Price.toString()
        binding.companyName.text = stockData.Reference_Company
        callStockHistory()
    }

    private fun callStockHistory() {
        mHomeViewModel.getStockHistory(oneDaySelected.toString(),
            oneWeekSelected.toString(),
            monthSelected.toString(),
            yearsSelected.toString(),
            spread,
            stockData.RIC)
    }

    private fun setGraphData() {

        binding.stockPriceDifference.text =
            "${String.format("%.2f", stockDifference)} (${String.format("%.2f", stockPercent)}%)"

        val xAxis: XAxis = binding.stockGraph.xAxis
        xAxis.axisMaximum = (dateList.size - 1).toFloat()
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        if (yearsSelected == 1) {
            xAxis.labelCount = 11
        } else if (yearsSelected == 5) {
            xAxis.labelCount = 11
        } else if (monthSelected == 1) {
            xAxis.labelCount = 9
        } else if (monthSelected == 6) {
            xAxis.labelCount = 9
        } else {
            xAxis.labelCount = 3
        }

        val leftAxis: YAxis = binding.stockGraph.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.axisMaximum = maxLatPrice.toFloat()



        leftAxis.setDrawZeroLine(false)
        leftAxis.setDrawLimitLinesBehindData(false)
        binding.stockGraph.axisRight.isEnabled = false
        binding.stockGraph.axisLeft.isEnabled = false
        binding.stockGraph.setScaleEnabled(false)
        setData()
    }

    private fun setData() {
        val values: ArrayList<Entry> = ArrayList()
        lastPrice.forEachIndexed { i, it ->
            values.add(Entry(i.toFloat(), it.toFloat()))
        }


        val lineSet = LineDataSet(values, "")
        lineSet.axisDependency = YAxis.AxisDependency.RIGHT
        lineSet.setDrawValues(false)
        lineSet.setDrawCircles(false)
        lineSet.setDrawFilled(true)
        lineSet.isHighlightEnabled = true
        lineSet.highlightLineWidth = 1F
        lineSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.grey)
        lineSet.setDrawHorizontalHighlightIndicator(false)

        lineSet.circleRadius = 5F


        if (stockDifference < 0) {
            val mv = YourMarkerView(requireActivity(), R.layout.markerview)
            mv.chartView = binding.stockGraph
            binding.stockGraph.marker = mv
            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.shareqrcolor))

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.graph_fade_color)
            lineSet.fillDrawable = drawable
            lineSet.color = ContextCompat.getColor(requireContext(), R.color.shareqrcolor)

        } else if (stockDifference >= 0) {
            val mv = YourMarkerView(requireActivity(), R.layout.markerview_green)
            mv.chartView = binding.stockGraph
            binding.stockGraph.marker = mv
            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.graphGreen))
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.graph_fade_green)
            lineSet.fillDrawable = drawable
            lineSet.color = ContextCompat.getColor(requireContext(), R.color.graphGreen)
        }

        val data = CombinedData()
        val dataSets = LineData()
        dataSets.addDataSet(lineSet)


        data.setData(dataSets) // set LineData...
        data.setData(generateBarData()) // set BarData...


        val barYMax: Float = generateBarData().yMax
        val leftAxis: YAxis = binding.stockGraph.axisLeft
        leftAxis.axisMaximum = barYMax * 4.5f


        binding.stockGraph.data = data
        setBarWidth()
        setXAxisValueFormatter()

        val rightAxis: YAxis = binding.stockGraph.axisRight
        rightAxis.spaceBottom = 65F

        binding.stockGraph.axisLeft.setDrawGridLines(false)
        binding.stockGraph.xAxis.setDrawGridLines(false)
        binding.stockGraph.xAxis.axisLineColor = Color.TRANSPARENT
        binding.stockGraph.description.isEnabled = false
        binding.stockGraph.legend.isEnabled = false
        binding.stockGraph.description.isEnabled = false
        binding.stockGraph.invalidate()
        binding.stockGraph.notifyDataSetChanged()
        binding.stockGraph.setPadding(0, 12, 0, 0)

        binding.stockGraph.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {


            override fun onValueSelected(e: Entry?, h: Highlight?) {
                lastPrice.forEachIndexed { i, it ->
                    if (e?.x?.toInt() == i) {


                        stockPercent = graphData[i].percentage_change_previous_entry.toDouble()
                        stockDifference = graphData[i].price_difference_previous_entry.toDouble()

                        val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy HH:mm")
                        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                        val myDate = simpleDateFormat.format(dateList[i])

                        binding.date.text = "$myDate GST"

                        binding.stockPriceDifference.text = "$stockDifference ($stockPercent%)"
                        binding.close.text = "${lastPrice[i]}"


                        /*val date = dateTimeFormat.format(dateList[i])
                        binding.date.text = "$date GST"

                        binding.stockPriceDifference.text = "${String.format("%.2f", stockDifference)} (${String.format("%.2f", stockPercent)}%)"
                        binding.close.text = "${lastPrice[i]}"*/

                        binding.stockDifferencesGroup.isVisible = true
                        if (stockDifference < 0) {
                            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(
                                requireContext(),
                                R.color.shareqrcolor))
                            binding.close.setTextColor(ContextCompat.getColor(
                                requireContext(),
                                R.color.shareqrcolor))

                        } else if (stockDifference >= 0) {

                            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(
                                requireContext(),
                                R.color.graphGreen))

                            binding.close.setTextColor(ContextCompat.getColor(
                                requireContext(),
                                R.color.graphGreen))

                        }
                    }
                }
            }

            override fun onNothingSelected() {}
        })


    }

    private fun setBarWidth() {

        if (oneDaySelected == 1) {
            binding.stockGraph.barData.barWidth = 0.02F

        } else if (oneWeekSelected == 1) {
            binding.stockGraph.barData.barWidth = 0.10F

        } else if (monthSelected == 1) {
            binding.stockGraph.barData.barWidth = 0.40F

        } else if (monthSelected == 6) {
            binding.stockGraph.barData.barWidth = 0.40F

        } else if (yearsSelected == 1) {
            binding.stockGraph.barData.barWidth = 0.90F

        } else if (yearsSelected == 5) {
            binding.stockGraph.barData.barWidth = 1.0F
        }

    }

    private fun setXAxisValueFormatter() {
        if (oneDaySelected == 1) {
            binding.stockGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatter(dateList)
        } else if (oneWeekSelected == 1) {
            binding.stockGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterOneWeek(dateList)
        } else if (monthSelected == 1) {
            binding.stockGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterOneMonth(dateList)
        } else if (monthSelected == 6) {
            binding.stockGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterSixMonth(dateList)
        } else if (yearsSelected == 1) {
            binding.stockGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterOneYear(dateList)
        } else if (yearsSelected == 5) {
            binding.stockGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterFiveYear(dateList)
        }

    }

    private fun generateBarData(): BarData {
        val values: ArrayList<BarEntry> = ArrayList()
        volumeList.forEachIndexed { i, it ->
            values.add(BarEntry(i.toFloat(), (it.toInt()).toFloat()))
        }


        val set1 = BarDataSet(values, "")
        set1.color = ContextCompat.getColor(requireContext(), R.color.barColor)
        set1.axisDependency = YAxis.AxisDependency.LEFT

        set1.setDrawValues(false)
        val barWidth = 0.15f
        val d = BarData(set1)
        d.isHighlightEnabled = false

        d.barWidth = barWidth
        return d
    }

    private fun setObserver() {
/*
        mHomeViewModel.stockHistory.observe(viewLifecycleOwner) { response ->
            if (response.isEmpty()) {
                setNoDataView()
            } else {
                binding.stockGraph.isVisible = true
                binding.noGraphData.isVisible = false

                binding.date.visibility = View.VISIBLE
                setStockResponse(response!!)
            }

        }*/

    }

    private fun setStockResponse(response: StockHistoryModel) {
        try {
            lastPrice.clear()
            volumeList.clear()
            dateList.clear()
            graphData = response
            graphData.forEach {
                if (it.Last_price.isEmpty()) {
                    lastPrice.add(0.0)
                } else {
                    lastPrice.add(it.Last_price.toDouble())
                }

                if (it.Volume.isEmpty()) {
                    volumeList.add(0.0)
                } else {
                    volumeList.add(it.Volume.toDouble())
                }

                dateList.add(listDateFormat.parse(it.Date_Time))
            }

            for (i in 0 until lastPrice.size) {
                if (lastPrice[i] > maxLatPrice) {
                    maxLatPrice = lastPrice[i].toInt()
                }
            }

            for (i in 0 until volumeList.size) {
                if (volumeList[i] > maxVolSize) {
                    maxVolSize = lastPrice[i].toInt()
                }
            }

            if (lastPrice.isNotEmpty()) {
                stockPercent = graphData[0].percentage_change_previous_entry.toDouble()
                stockDifference = graphData[0].price_difference_previous_entry.toDouble()
            }
            selectedDate = "${dateFormat.format(dateList[0])} - ${dateFormat.format(dateList[dateList.size - 1])}"
            binding.date.text = selectedDate

            requireActivity().runOnUiThread {
                setGraphData()
            }
        }catch (e:Exception){
            println("got an exception $e")
        }


    }

    private fun setNoDataView() {
        binding.noGraphData.isVisible = true
        binding.stockGraph.visibility = View.INVISIBLE
        binding.date.visibility = View.INVISIBLE
    }


    private fun setOnClickListener() {
        binding.closeScreen.setOnClickListener(this@FullStockGraphFragment)
        binding.oneDay.setOnClickListener(this@FullStockGraphFragment)
        binding.oneWeek.setOnClickListener(this@FullStockGraphFragment)
        binding.oneMonth.setOnClickListener(this@FullStockGraphFragment)
        binding.sixMonth.setOnClickListener(this@FullStockGraphFragment)
        binding.oneYear.setOnClickListener(this@FullStockGraphFragment)
        binding.fiveYear.setOnClickListener(this@FullStockGraphFragment)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeScreen -> {
                findNavController().popBackStack()
            }
            R.id.oneDay -> {
                setFilterSelection(binding.oneDay)
                oneDaySelected = 1
                oneWeekSelected = 0
                monthSelected = 0
                yearsSelected = 0
                spread = "A"
//                binding.date.text = selectedDate
                binding.stockGraph.highlightValue(null)
                binding.stockDifferencesGroup.isVisible = false
                callStockHistory()
            }

            R.id.oneWeek -> {
                setFilterSelection(binding.oneWeek)
                oneDaySelected = 0
                oneWeekSelected = 1
                monthSelected = 0
                yearsSelected = 0
                spread = "A"
//                setSelectedDates(7, "days")
                binding.stockGraph.highlightValue(null)
                binding.stockDifferencesGroup.isVisible = false
                callStockHistory()
            }
            R.id.oneMonth -> {
                setFilterSelection(binding.oneMonth)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 1
                yearsSelected = 0
                spread = "A"
//                setSelectedDates(1, "month")
                binding.stockGraph.highlightValue(null)
                binding.stockDifferencesGroup.isVisible = false
                callStockHistory()
            }

            R.id.sixMonth -> {
                setFilterSelection(binding.sixMonth)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 6
                yearsSelected = 0
                spread = "E"
//                setSelectedDates(6, "month")
                binding.stockGraph.highlightValue(null)
                binding.stockDifferencesGroup.isVisible = false
                callStockHistory()
            }

            R.id.oneYear -> {
                setFilterSelection(binding.oneYear)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 0
                yearsSelected = 1
                spread = "E"
//                setSelectedDates(12, "month")
                binding.stockGraph.highlightValue(null)
                binding.stockDifferencesGroup.isVisible = false
                callStockHistory()
            }
            R.id.fiveYear -> {
                setFilterSelection(binding.fiveYear)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 0
                yearsSelected = 5
                spread = "E"
//                setSelectedDates(60, "month")
                binding.stockGraph.highlightValue(null)
                binding.stockDifferencesGroup.isVisible = false
                callStockHistory()
            }
        }
    }

    private fun setSelectedDates(i: Int, type: String) {
        if (type == "days") {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val newDate = calendar.time
            binding.date.text = "${dateFormat.format(newDate)} - $selectedDate"
        } else {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            val newDate = calendar.time
            binding.date.text = "${dateFormat.format(newDate)} - $selectedDate"
        }
    }

    private fun setFilterSelection(view: TextView) {
        binding.oneDay.background = null
        binding.oneWeek.background = null
        binding.oneMonth.background = null
        binding.sixMonth.background = null
        binding.oneYear.background = null
        binding.fiveYear.background = null
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
    }

    private fun setFilterSelectionFromPrevious() {
        binding.oneDay.background = null
        binding.oneWeek.background = null
        binding.oneMonth.background = null
        binding.sixMonth.background = null
        binding.oneYear.background = null
        binding.fiveYear.background = null
        if (oneDaySelected == 1) {
            binding.oneDay.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
        } else if (oneWeekSelected == 1) {
            binding.oneWeek.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
        } else if (monthSelected == 1) {
            binding.oneMonth.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
        } else if (monthSelected == 6) {
            binding.sixMonth.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
        } else if (yearsSelected == 1) {
            binding.oneYear.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
        } else if (yearsSelected == 5) {
            binding.fiveYear.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
        }


    }
}

