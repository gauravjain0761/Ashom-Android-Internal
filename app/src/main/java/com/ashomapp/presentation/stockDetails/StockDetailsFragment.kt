package com.ashomapp.presentation.stockDetails

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.FragmentStockDetailsBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.presentation.home.onCountryClick
import com.ashomapp.presentation.home.stockPrice.model.StockHistoryModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import com.ashomapp.presentation.search.SearchViewModel
import com.ashomapp.presentation.stockDetails.adapter.StockNewsAdapter
import com.ashomapp.utils.YourMarkerView
import com.ashomapp.utils.loadImageFromUrl
import com.bumptech.glide.Glide
import com.doctorsplaza.app.utils.Resource
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow


class StockDetailsFragment : Fragment(R.layout.fragment_stock_details), onCountryClick,
    View.OnClickListener {
    private lateinit var companyDTO: CompanyDTO
    private var industry: String = ""
    private lateinit var graphData: StockHistoryModel
    private var symbolTicker: String = ""
    private var countryName: String = ""

    private var stockDifference: Double = 0.0
    private var stockPercent: Double = 0.0
    private var maxVolSize: Int = 0
    private var maxLatPrice: Int = 0
    private lateinit var stockData: StockListModelItem
    private lateinit var binding: FragmentStockDetailsBinding

    private var currentView: View? = null

    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private val mSearchViewModel by activityViewModels<SearchViewModel>()

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
    var pageNo = 0
    private var stockNewsAdapter: StockNewsAdapter = StockNewsAdapter()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_stock_details, container, false)
            binding = FragmentStockDetailsBinding.bind(currentView!!)
            init()
            setObserver()
            setAdapter()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun init() {

        binding.stockDataViews.visibility = View.INVISIBLE
        binding.stockPrice.visibility = View.INVISIBLE

        binding.loaderImage.isVisible = true
        loadImageFromUrl(binding.loaderImage, 0)

        dateFormat.timeZone = TimeZone.getTimeZone("GST")
        listDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        selectedDate = dateFormat.format(Date())
        countryName = arguments?.getString("countryCode").toString()
        symbolTicker = if(arguments?.getString("symbolTicker").toString()!="null"){
            arguments?.getString("symbolTicker").toString()
        }else{
            ""
        }
        industry = arguments?.getString("industry").toString()
        val gson = Gson()
        stockData = gson.fromJson(arguments?.getString("stockData"), StockListModelItem::class.java)

        if (arguments?.getString("companyDTO")
                .toString() != "null"
        ) {
            companyDTO = gson.fromJson(arguments?.getString("companyDTO"), CompanyDTO::class.java)
        }

        mHomeViewModel.getStockCompanyNews(pageNo,
            companyName = symbolTicker,
            countryName = countryName)


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


        val anim: Animation = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 500
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.financialReportsLbl.startAnimation(anim)

        when (countryName) {
            "KSA" -> {
                binding.countryFlag.setImageResource(R.drawable.ksaflag)
            }
            "UAE" -> {
                binding.countryFlag.setImageResource(R.drawable.uae_flag)
            }
            "All Countries" -> {
                binding.countryFlag.setImageResource(R.drawable.all_countries)
            }
            else -> {
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load("https://countryflagsapi.com/png/${countryName}")
                    .into(binding.countryFlag)
            }
        }

    }

    private fun setStockData() {
        binding.open.text = stockData.Open.toString()
        binding.high.text = stockData.High.toString()
        binding.low.text = stockData.Low.toString()
        binding.vol.text = getFormattedNumber(stockData.Volume.toLong())
        binding.last.text = stockData.Last.toString()
        binding.close.text = stockData.Universal_Close_Price.toString()
        binding.stockPrice.text = "$currencySymbol ${stockData.Last}"
        binding.companyName.text = stockData.Reference_Company

        if (this::companyDTO.isInitialized) {
            binding.companySector.text = "Sector: $industry"
            binding.companySymbol.text = "Symbol: $symbolTicker"
        } else {
            binding.companySector.text = ""
            binding.companySymbol.text = ""
        }

        binding.stockPrice.visibility = View.VISIBLE
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


        val stockDifferenceDecimalPlaces =
            abs(stockDifference).toString().length - abs(stockDifference).toString()
                .indexOf('.') - 1

        val stockPercentDecimalPlaces =
            abs(stockDifference).toString().length - abs(stockDifference).toString()
                .indexOf('.') - 1


        if (stockDifference.toString()
                .endsWith("0") || stockDifferenceDecimalPlaces < 3 && stockDifferenceDecimalPlaces < 3
        ) {
            binding.stockPriceDifference.text = "${String.format("%.2f", stockDifference)} (${
                String.format("%.2f",
                    stockPercent)
            }%)"

        } else if (stockDifference.toString().endsWith("0") || stockDifferenceDecimalPlaces < 3) {
            binding.stockPriceDifference.text = "${String.format("%.2f", stockDifference)} (${
                String.format("%.3f",
                    stockPercent)
            }%)"

        } else if (stockPercent.toString().endsWith("0") || stockPercentDecimalPlaces < 3) {
            binding.stockPriceDifference.text = "${String.format("%.3f", stockDifference)} (${
                String.format("%.2f",
                    stockPercent)
            }%)"
        } else {

            binding.stockPriceDifference.text = "${String.format("%.3f", stockDifference)} (${
                String.format("%.3f",
                    stockPercent)
            }%)"
        }

        val xAxis: XAxis = binding.barChartGraph.xAxis
//        xAxis.axisMaximum = (dateList.size - 1).toFloat()
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        /*if (yearsSelected == 1) {
            xAxis.labelCount  = 11
        } else if (yearsSelected == 5) {
            xAxis.labelCount = 11
        } else if (monthSelected == 1) {
            xAxis.labelCount = 9
        } else if (monthSelected == 6) {
            xAxis.labelCount = 9
        } else {
            xAxis.labelCount = 3
        }*/
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
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
        lineSet.lineWidth = 1.5F
        lineSet.highlightLineWidth = 1F
        lineSet.isHighlightEnabled = true
        lineSet.setDrawHorizontalHighlightIndicator(false)
        lineSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.grey)


        volumeList.forEachIndexed { i, it ->
            barValues.add(BarEntry(i.toFloat(),
                (it.toInt()).toFloat()))
        }


        val barSet = BarDataSet(barValues, "")
        barSet.color = ContextCompat.getColor(requireContext(), R.color.barColor)
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
        binding.lineGraph.xAxis.isEnabled = false
        binding.lineGraph.xAxis.axisLineColor = Color.TRANSPARENT
        binding.lineGraph.description.isEnabled = false
        binding.lineGraph.legend.isEnabled = false
        binding.lineGraph.setOnChartValueSelectedListener(lineChartSelectionListener)


        binding.barChartGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChartGraph.axisLeft.setDrawGridLines(false)
        binding.barChartGraph.xAxis.setDrawGridLines(false)
        binding.barChartGraph.xAxis.axisLineColor = Color.TRANSPARENT
        binding.barChartGraph.description.isEnabled = false
        binding.barChartGraph.legend.isEnabled = false
        binding.barChartGraph.setFitBars(true)
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
                    binding.pastWeekLbl.visibility = View.INVISIBLE
                    val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy hh:mm a")
                    simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                    val myDate = simpleDateFormat.format(dateList[i])
                    binding.date.text = "$myDate GST"
                    binding.stockPriceDifference.text = "$stockDifference ($stockPercent%)"
                    binding.stockPrice.text = "$currencySymbol ${lastPrice[i]}"

                    if (stockDifference < 0) {
                        binding.stockUpDownArrow.setImageResource(R.drawable.stock_down)
                        binding.stockUpDownArrow.setColorFilter(ContextCompat.getColor(
                            requireContext(),
                            R.color.shareqrcolor), android.graphics.PorterDuff.Mode.SRC_IN)
                        binding.stockPriceDifference.setTextColor(ContextCompat.getColor(
                            requireContext(),
                            R.color.shareqrcolor))

                    } else if (stockDifference >= 0) {
                        binding.stockUpDownArrow.setImageResource(R.drawable.stock_up)
                        binding.stockUpDownArrow.setColorFilter(ContextCompat.getColor(
                            requireContext(),
                            R.color.graphGreen), android.graphics.PorterDuff.Mode.SRC_IN)
                        binding.stockPriceDifference.setTextColor(ContextCompat.getColor(
                            requireContext(),
                            R.color.graphGreen))
                    }
                }
            }
        }

        override fun onNothingSelected() {}

    }

    private fun setGraphColor(lineSet: LineDataSet) {
        if (stockDifference < 0) {
            val mv = YourMarkerView(requireActivity(), R.layout.markerview)
            mv.chartView = binding.lineGraph
            binding.lineGraph.marker = mv

            binding.stockUpDownArrow.setImageResource(R.drawable.stock_down)
            binding.stockUpDownArrow.setColorFilter(ContextCompat.getColor(requireContext(),
                R.color.shareqrcolor), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.shareqrcolor))

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.graph_fade_color)
            lineSet.fillDrawable = drawable
            lineSet.color = ContextCompat.getColor(requireContext(), R.color.shareqrcolor)

        } else if (stockDifference >= 0) {
            val mv = YourMarkerView(requireActivity(), R.layout.markerview_green)
            mv.chartView = binding.lineGraph
            binding.lineGraph.marker = mv

            binding.stockUpDownArrow.setImageResource(R.drawable.stock_up)
            binding.stockUpDownArrow.setColorFilter(ContextCompat.getColor(requireContext(),
                R.color.graphGreen), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.stockPriceDifference.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.graphGreen))
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.graph_fade_green)
            lineSet.fillDrawable = drawable
            lineSet.color = ContextCompat.getColor(requireContext(), R.color.graphGreen)
        }
    }

    private fun setBarWidth() {
        if (oneDaySelected == 1) {
            binding.barChartGraph.barData.barWidth = 0.035F
        } else if (oneWeekSelected == 1) {
            binding.barChartGraph.barData.barWidth = 0.15F
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

        binding.barChartGraph.xAxis.setLabelCount(4, false)
        if (oneDaySelected == 1) {
            if (dateList.size > 4) {
                val tempDateList: MutableList<Date?> = ArrayList()
                dateList.forEachIndexed { index, it ->
                    if (index % 2 == 0) {
                        tempDateList.add(it)
                    } else {
                        tempDateList.add(null)
                    }
                }

                binding.barChartGraph.xAxis.valueFormatter =
                    IndexAxisValueFormatter(tempDateList.map {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        timeFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                        if (it != null) {
                            timeFormat.format(it)
                        } else {
                            ""
                        }
                    })
            } else {

                binding.barChartGraph.xAxis.valueFormatter = IndexAxisValueFormatter(dateList.map {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    timeFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                    timeFormat.format(it)
                })
            }

        } else if (oneWeekSelected == 1) {
            binding.barChartGraph.xAxis.valueFormatter = IndexAxisValueFormatter(dateList.map {
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

                dateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                dateFormat.format(it)
            })
        } else if (monthSelected == 1) {
            binding.barChartGraph.xAxis.valueFormatter = IndexAxisValueFormatter(dateList.map {
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

                dateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                dateFormat.format(it)
            })
        } else if (monthSelected == 6) {
            binding.barChartGraph.xAxis.valueFormatter = IndexAxisValueFormatter(dateList.map {
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

                dateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                dateFormat.format(it)
            })
        } else if (yearsSelected == 1) {
            binding.barChartGraph.xAxis.valueFormatter = IndexAxisValueFormatter(dateList.map {
                val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                dateFormat.format(it)
            })
        } else if (yearsSelected == 5) {
            binding.barChartGraph.xAxis.valueFormatter = IndexAxisValueFormatter(dateList.map {
                val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("GMT+4")
                dateFormat.format(it)
            })
        }
    }


    private fun setObserver() {
        mHomeViewModel.stockCompanyNews.observe(viewLifecycleOwner) { response ->
            requireActivity().runOnUiThread {
                if (response.data.isEmpty()) {
                    binding.newLbl.text = "No News Available"
                } else {
                    stockNewsAdapter.differ.submitList(response.data)
                    binding.newsRv.apply {
                        setHasFixedSize(true)
                        isNestedScrollingEnabled = false
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = stockNewsAdapter
                    }
                }
            }
        }

        mHomeViewModel.stockHistory.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.loaderImage.isVisible = false
                    if (response.data?.isNotEmpty() == true) {

                        binding.noStockData.isVisible = false
                        binding.stockDataViews.visibility = View.VISIBLE
                        binding.stockPrice.visibility = View.VISIBLE
                        setStockResponse(response.data)
                    } else {
                        setNoDataView()
                    }
                }
                is Resource.Loading -> {
                    loadImageFromUrl(binding.loaderImage, 0)
                }
                is Resource.Error -> {
                    binding.loaderImage.isVisible = false
                    setNoDataView()
                }
            }
        }
    }

    private fun setNoDataView() {
        binding.noStockData.isVisible = true
        binding.stockPrice.visibility = View.VISIBLE
        binding.stockDataViews.visibility = View.INVISIBLE
    }


    private fun setStockResponse(response: StockHistoryModel?) {
        lastPrice.clear()
        volumeList.clear()
        dateList.clear()
        graphData = response!!
        graphData.forEach {
            try {
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

                listDateFormat.parse(it.Date_Time)?.let { it1 -> dateList.add(it1) }

            } catch (e: Exception) {
                println("Data Adding Exception $e   ${it.Volume}")
            }
        }

        try {
            selectedDate =
                "${dateFormat.format(dateList[0])} - ${dateFormat.format(dateList[dateList.size - 1])}"
            binding.date.text = selectedDate

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

                if (lastPrice.size == 1) {
                    stockPercent = graphData[0].percentage_change_previous_entry.toDouble()
                    stockDifference = graphData[0].price_difference_previous_entry.toDouble()
                } else {

                    stockPercent = ((lastPrice[lastPrice.size - 1] - lastPrice[0]) * 100) / lastPrice[lastPrice.size - 1]
                    stockDifference = lastPrice[lastPrice.size - 1] - lastPrice[0]
                }

            }

            requireActivity().runOnUiThread {
                setGraphData()
            }
        } catch (e: Exception) {
            println("Data Setup Exception $e")
        }
    }

    private fun setAdapter() {

        stockNewsAdapter.setOnStockNewsClickListener {
            val newstostring = Gson().toJson(it)
            HomeFlow.sectionBottomID = R.id.stockDetailsFragment
            val args = bundleOf("Newsdetail" to newstostring)
            findNavController().navigate(R.id.newsDetail, args)
        }

        stockNewsAdapter.setOnNewTitleClickListener { }

        stockNewsAdapter.setOnStockNewsShareClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT,
                "Hi, I am using Ashom.app. Feel free to download ( https://play.google.com/store/apps/details?id=com.ashomapp ) \n\nNews: \n${it.title}\n${it.link}")
            startActivity(Intent.createChooser(intent, "Share with:"))
        }

        stockNewsAdapter.setOnStockNewsShareInForumClickListener {
            mHomeViewModel.recordEvent("create_news_forum")
            val companydetail = Gson().toJson(it)
            val args = bundleOf("news_item_to_forum" to companydetail)
            findNavController().navigate(R.id.composeFrag, args)
        }

    }

    private fun setOnClickListener() {
        binding.backArrow.setOnClickListener(this@StockDetailsFragment)
        binding.oneDay.setOnClickListener(this@StockDetailsFragment)
        binding.oneWeek.setOnClickListener(this@StockDetailsFragment)
        binding.oneMonth.setOnClickListener(this@StockDetailsFragment)
        binding.sixMonth.setOnClickListener(this@StockDetailsFragment)
        binding.oneYear.setOnClickListener(this@StockDetailsFragment)
        binding.fiveYear.setOnClickListener(this@StockDetailsFragment)
        binding.expand.setOnClickListener(this@StockDetailsFragment)
        binding.financialReportsLbl.setOnClickListener(this@StockDetailsFragment)


    }

    override fun onCountryClick(countriesDTO: CountriesDTO, view: View, position: Int) {}

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.backArrow -> {
                findNavController().popBackStack()
            }

            R.id.oneDay -> {
                setFilterSelection(binding.oneDay)
                oneDaySelected = 1
                oneWeekSelected = 0
                monthSelected = 0
                yearsSelected = 0
                spread = "A"
                binding.pastWeekLbl.text = ""
                binding.lineGraph.highlightValue(null)
                callStockHistory()

            }

            R.id.oneWeek -> {
                setFilterSelection(binding.oneWeek)
                oneDaySelected = 0
                oneWeekSelected = 1
                monthSelected = 0
                yearsSelected = 0
                spread = "A"
                binding.pastWeekLbl.text = "Past Week"
                binding.lineGraph.highlightValue(null)
                callStockHistory()
            }

            R.id.oneMonth -> {
                setFilterSelection(binding.oneMonth)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 1
                yearsSelected = 0
                spread = "A"
                binding.pastWeekLbl.text = "Past 1 Month"
                binding.lineGraph.highlightValue(null)
                callStockHistory()
            }

            R.id.sixMonth -> {
                setFilterSelection(binding.sixMonth)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 6
                yearsSelected = 0
                spread = "E"
                binding.pastWeekLbl.text = "Past 6 Month"
                binding.lineGraph.highlightValue(null)
                callStockHistory()
            }

            R.id.oneYear -> {
                setFilterSelection(binding.oneYear)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 0
                yearsSelected = 1
                spread = "E"
                binding.pastWeekLbl.text = "Past 1 Year"
                binding.lineGraph.highlightValue(null)
                callStockHistory()
            }
            R.id.fiveYear -> {
                binding.pastWeekLbl.isVisible = true
                setFilterSelection(binding.fiveYear)
                oneDaySelected = 0
                oneWeekSelected = 0
                monthSelected = 0
                yearsSelected = 5
                spread = "E"
                binding.pastWeekLbl.text = "Past 5 Years"
                binding.lineGraph.highlightValue(null)
                callStockHistory()
            }
            R.id.expand -> {
                val bundle = Bundle()
                val gson = Gson()
                val json = gson.toJson(stockData)
                bundle.putString("stockData", json)
                bundle.putInt("oneDaySelected", oneDaySelected)
                bundle.putInt("oneWeekSelected", oneWeekSelected)
                bundle.putInt("monthSelected", monthSelected)
                bundle.putInt("yearsSelected", yearsSelected)

//                findNavController().navigate(R.id.fullStockGraphFragment, bundle)


                startActivity(Intent(requireActivity(), FullStockGraphActivity::class.java).apply {
                    putExtra("stockData", json)
                    putExtra("oneDaySelected", oneDaySelected.toString())
                    putExtra("oneWeekSelected", oneWeekSelected.toString())
                    putExtra("monthSelected", monthSelected.toString())
                    putExtra("yearsSelected", yearsSelected.toString())
                })
            }

            R.id.financialReportsLbl -> {
                if (this::companyDTO.isInitialized) {
                    recordCompany(countryName, symbolTicker)
                    mSearchViewModel.getSearchStr("CompanyName✂${companyDTO.Country}✂${companyDTO.SymbolTicker}✂${companyDTO.image}✂${companyDTO.Company_Name}✂${companyDTO.id}")
                    mHomeViewModel.recordEvent("company_page_view")
                    val companyDetail = Gson().toJson(companyDTO)
                    val args = Bundle()
                    args.putString("company_detail", companyDetail)
                    findNavController().navigate(R.id.companyDetail, args)
                }else{
                    Toast.makeText(requireContext(), "Financial reports are not available.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun recordCompany(country: String, symbolTicker: String) {
        when (country) {
            "KSA" -> {
                mHomeViewModel.recordEvent("view_ksa_$symbolTicker")
            }
            "Kuwait" -> {
                mHomeViewModel.recordEvent("view_kuwait_$symbolTicker")
            }
            "Qatar" -> {
                mHomeViewModel.recordEvent("view_qatar_$symbolTicker")
            }
            "Oman" -> {
                mHomeViewModel.recordEvent("view_oman_$symbolTicker")
            }
            "UAE" -> {
                mHomeViewModel.recordEvent("view_uae_$symbolTicker")
            }
            "Bahrain" -> {
                mHomeViewModel.recordEvent("view_bahrain_$symbolTicker")
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
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.corner_radius)
    }

    override fun onResume() {
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setObserver()
        super.onResume()
    }

    fun getFormattedNumber(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
        return String.format("%.1f %c", count / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
    }
}

