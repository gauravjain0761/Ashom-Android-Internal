package com.ashomapp.presentation.stockDetails

import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ashomapp.R
import com.ashomapp.databinding.ActivityFullStockGraphBinding
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.presentation.home.stockPrice.model.StockHistoryModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import com.ashomapp.utils.*
import com.doctorsplaza.app.utils.Resource
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class FullStockGraphActivity : AppCompatActivity(), View.OnClickListener {
    private var stockDifference: Double = 0.0
    private var stockPercent: Double = 0.0

    private var maxVolSize: Int = 0
    private var maxLatPrice: Int = 0
    private lateinit var stockData: StockListModelItem
    private lateinit var binding: ActivityFullStockGraphBinding

    private var currentView: View? = null

    private val mHomeViewModel: HomeViewModel by viewModels()

    private lateinit var graphData: StockHistoryModel

    private var oneDaySelected = 1
    private var oneWeekSelected = 0
    private var monthSelected = 0
    private var yearsSelected = 0
    private var spread = "A"
    private var dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var listDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private var selectedDate = dateFormat.format(Date())

    private var lastPrice: MutableList<Double> = ArrayList()
    private var volumeList: MutableList<Double> = ArrayList()
    private var dateList: MutableList<Date> = ArrayList()

    private var currencySymbol = "SR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        binding = ActivityFullStockGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setObserver()
        setOnClickListener()
    }


    private fun init() {
//        val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("GST")
        val gson = Gson()
        stockData =
            gson.fromJson(intent.getStringExtra("stockData"), StockListModelItem::class.java)

        oneDaySelected = intent.getStringExtra("oneDaySelected")!!.toInt()
        oneWeekSelected = intent.getStringExtra("oneWeekSelected")!!.toInt()
        monthSelected = intent.getStringExtra("monthSelected")!!.toInt()
        yearsSelected = intent.getStringExtra("yearsSelected")!!.toInt()
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

        binding.stockPriceDifference.text = "${String.format("%.3f", stockDifference)} (${String.format("%.3f", stockPercent)}%)"

        val xAxis: XAxis = binding.barChartGraph.xAxis
        xAxis.axisMaximum = (dateList.size - 1).toFloat()
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        if (yearsSelected == 1) {
            xAxis.labelCount  = 11
        } else if (yearsSelected == 5) {
            xAxis.labelCount = 11
        } else if (monthSelected == 1) {
            xAxis.labelCount = 9
        } else if (monthSelected == 6) {
            xAxis.labelCount = 9
        } else {
            xAxis.labelCount = 3
        }

        xAxis.setAvoidFirstLastClipping(true)

        binding.lineGraph.axisRight.isEnabled = false
        binding.lineGraph.axisLeft.isEnabled = false
        binding.lineGraph.setScaleEnabled(false)


        binding.barChartGraph.axisRight.isEnabled = false
        binding.barChartGraph.axisLeft.isEnabled = false
        binding.barChartGraph.setScaleEnabled(false)
        setData()
    }

    private fun setData() {
        val lineValues: ArrayList<Entry> = ArrayList()

        val barValues: ArrayList<BarEntry> = ArrayList()

        lastPrice.forEachIndexed { i, it -> lineValues.add(Entry(i.toFloat(), it.toFloat())) }

        val lineSet = LineDataSet(lineValues, "")
        lineSet.axisDependency = YAxis.AxisDependency.RIGHT
        lineSet.setDrawFilled(true)
        lineSet.setDrawValues(false)
        lineSet.setDrawValues(false)
        lineSet.setDrawCircles(false)
        lineSet.lineWidth = 2.0F
        lineSet.highlightLineWidth = 1F
        lineSet.isHighlightEnabled = true
        lineSet.setDrawHorizontalHighlightIndicator(false)
        lineSet.highLightColor = ContextCompat.getColor(this, R.color.grey)


        volumeList.forEachIndexed { i, it -> barValues.add(BarEntry(i.toFloat(), (it.toInt()).toFloat())) }


        val barSet = BarDataSet(barValues, "")
        barSet.color = ContextCompat.getColor(this, R.color.barColor)
        barSet.axisDependency = YAxis.AxisDependency.LEFT
        barSet.setDrawValues(false)
        barSet.isHighlightEnabled = false


        val barData = BarData(barSet)
        val lineData = LineData()

        lineData.addDataSet(lineSet)


        binding.lineGraph.data = lineData
        binding.barChartGraph.data = barData

        setBarWidth()
        setGraphColor(lineSet)
        setXAxisValueFormatter()


        binding.lineGraph.axisLeft.setDrawGridLines(false)
        binding.lineGraph.xAxis.setDrawGridLines(false)
        binding.lineGraph.xAxis.isEnabled=false
        binding.lineGraph.xAxis.axisLineColor = Color.TRANSPARENT
        binding.lineGraph.description.isEnabled = false
        binding.lineGraph.legend.isEnabled = false
        binding.lineGraph.setOnChartValueSelectedListener(lineChartSelectionListener)


        binding.barChartGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChartGraph.setFitBars(true)
        binding.barChartGraph.axisLeft.setDrawGridLines(false)
        binding.barChartGraph.xAxis.setDrawGridLines(false)
        binding.barChartGraph.xAxis.axisLineColor = Color.TRANSPARENT
        binding.barChartGraph.description.isEnabled = false
        binding.barChartGraph.legend.isEnabled = false
        binding.barChartGraph.setVisibleXRangeMinimum(barValues.size.toFloat())

        binding.barChartGraph.invalidate()
        binding.lineGraph.invalidate()

        binding.barChartGraph.notifyDataSetChanged()
        binding.lineGraph.notifyDataSetChanged()
    }

    private val lineChartSelectionListener = object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            lastPrice.forEachIndexed { i, _ ->
                if (e?.x?.toInt() == i) {

                    stockPercent = graphData[i].percentage_change_previous_entry.toDouble()
                    stockDifference = graphData[i].price_difference_previous_entry.toDouble()

                    val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy hh:mm a")
                    simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                    val myDate = simpleDateFormat.format(dateList[i])

                    binding.date.text = "$myDate GST"

                    binding.stockPriceDifference.text = "$stockDifference ($stockPercent%)"
                    binding.close.text = "${lastPrice[i]}"

                    binding.stockDifferencesGroup.isVisible = true
                    if (stockDifference < 0) {
                        binding.stockPriceDifference.setTextColor(ContextCompat.getColor(
                            this@FullStockGraphActivity,
                            R.color.shareqrcolor))
                        binding.close.setTextColor(ContextCompat.getColor(
                            this@FullStockGraphActivity,
                            R.color.shareqrcolor))

                    } else if (stockDifference >= 0) {

                        binding.stockPriceDifference.setTextColor(ContextCompat.getColor(
                            this@FullStockGraphActivity,
                            R.color.graphGreen))

                        binding.close.setTextColor(ContextCompat.getColor(
                            this@FullStockGraphActivity,
                            R.color.graphGreen))

                    }
                }

            }
        }
        override fun onNothingSelected() {}

    }

    private fun setGraphColor(lineSet: LineDataSet) {
        if (stockDifference < 0) {
            val mv = YourMarkerView(this, R.layout.markerview)
            mv.chartView = binding.lineGraph
            binding.lineGraph.marker = mv
            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(this,
                R.color.shareqrcolor))

            val drawable = ContextCompat.getDrawable(this, R.drawable.graph_fade_color)
            lineSet.fillDrawable = drawable
            lineSet.color = ContextCompat.getColor(this, R.color.shareqrcolor)

        } else if (stockDifference >= 0) {
            val mv = YourMarkerView(this, R.layout.markerview_green)
            mv.chartView = binding.lineGraph
            binding.lineGraph.marker = mv
            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(this,
                R.color.graphGreen))
            val drawable = ContextCompat.getDrawable(this, R.drawable.graph_fade_green)
            lineSet.fillDrawable = drawable
            lineSet.color = ContextCompat.getColor(this, R.color.graphGreen)
        }
    }

    private fun setBarWidth() {

        if (oneDaySelected == 1) {
            binding.barChartGraph.barData.barWidth = 0.02F

        } else if (oneWeekSelected == 1) {
            binding.barChartGraph.barData.barWidth = 0.10F

        } else if (monthSelected == 1) {
            binding.barChartGraph.barData.barWidth = 0.40F

        } else if (monthSelected == 6) {
            binding.barChartGraph.barData.barWidth = 0.40F

        } else if (yearsSelected == 1) {
            binding.barChartGraph.barData.barWidth = 0.90F

        } else if (yearsSelected == 5) {
            binding.barChartGraph.barData.barWidth = 1.0F
        }

    }

    private fun setXAxisValueFormatter() {
        if (oneDaySelected == 1) {
            binding.barChartGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatter(dateList)
        } else if (oneWeekSelected == 1) {
            binding.barChartGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterOneWeek(dateList)
        } else if (monthSelected == 1) {
            binding.barChartGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterOneMonth(dateList)
        } else if (monthSelected == 6) {
            binding.barChartGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterSixMonth(dateList)
        } else if (yearsSelected == 1) {
            binding.barChartGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterOneYear(dateList)
        } else if (yearsSelected == 5) {
            binding.barChartGraph.xAxis.valueFormatter = ClaimsXAxisValueFormatterFiveYear(dateList)
        }

    }



    private fun setObserver() {

        mHomeViewModel.stockHistory.observe(this) { response ->
            when(response){
                is Resource.Success->{
                    if(response.data?.isNotEmpty() == true){

                        binding.lineGraph.isVisible = true
                        binding.barChartGraph.isVisible = true
                        binding.noGraphData.isVisible = false
                        binding.date.visibility = View.VISIBLE

                        setStockResponse(response.data)
                    }else{
                        setNoDataView()
                    }
                }
                is Resource.Loading->{
                    setNoDataView()
                }
                is Resource.Error->{
                    setNoDataView()
                }
            }


        }

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
            selectedDate =
                "${dateFormat.format(dateList[0])} - ${dateFormat.format(dateList[dateList.size - 1])}"
            binding.date.text = selectedDate

            runOnUiThread {
                setGraphData()
            }
        } catch (e: Exception) {
            println("got an exception $e")
        }


    }

    private fun setNoDataView() {
        binding.noGraphData.isVisible = true
        binding.lineGraph.visibility = View.INVISIBLE
        binding.barChartGraph.visibility = View.INVISIBLE
        binding.date.visibility = View.INVISIBLE
    }


    private fun setOnClickListener() {
        binding.closeScreen.setOnClickListener(this@FullStockGraphActivity)
        binding.oneDay.setOnClickListener(this@FullStockGraphActivity)
        binding.oneWeek.setOnClickListener(this@FullStockGraphActivity)
        binding.oneMonth.setOnClickListener(this@FullStockGraphActivity)
        binding.sixMonth.setOnClickListener(this@FullStockGraphActivity)
        binding.oneYear.setOnClickListener(this@FullStockGraphActivity)
        binding.fiveYear.setOnClickListener(this@FullStockGraphActivity)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeScreen -> {
                onBackPressed()
            }
            R.id.oneDay -> {
                setFilterSelection(binding.oneDay)
                oneDaySelected = 1
                oneWeekSelected = 0
                monthSelected = 0
                yearsSelected = 0
                spread = "A"
                binding.lineGraph.highlightValue(null)
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

                binding.lineGraph.highlightValue(null)
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
                binding.lineGraph.highlightValue(null)
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
                binding.lineGraph.highlightValue(null)
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
                binding.lineGraph.highlightValue(null)
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
                binding.lineGraph.highlightValue(null)
                binding.stockDifferencesGroup.isVisible = false
                callStockHistory()
            }
        }
    }


    private fun setFilterSelection(view: TextView) {
        binding.oneDay.background = null
        binding.oneWeek.background = null
        binding.oneMonth.background = null
        binding.sixMonth.background = null
        binding.oneYear.background = null
        binding.fiveYear.background = null
        view.background =
            ContextCompat.getDrawable(this@FullStockGraphActivity, R.drawable.corner_radius)
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
                ContextCompat.getDrawable(this@FullStockGraphActivity, R.drawable.corner_radius)
        } else if (oneWeekSelected == 1) {
            binding.oneWeek.background =
                ContextCompat.getDrawable(this@FullStockGraphActivity, R.drawable.corner_radius)
        } else if (monthSelected == 1) {
            binding.oneMonth.background =
                ContextCompat.getDrawable(this@FullStockGraphActivity, R.drawable.corner_radius)
        } else if (monthSelected == 6) {
            binding.sixMonth.background =
                ContextCompat.getDrawable(this@FullStockGraphActivity, R.drawable.corner_radius)
        } else if (yearsSelected == 1) {
            binding.oneYear.background =
                ContextCompat.getDrawable(this@FullStockGraphActivity, R.drawable.corner_radius)
        } else if (yearsSelected == 5) {
            binding.fiveYear.background =
                ContextCompat.getDrawable(this@FullStockGraphActivity, R.drawable.corner_radius)
        }


    }
}