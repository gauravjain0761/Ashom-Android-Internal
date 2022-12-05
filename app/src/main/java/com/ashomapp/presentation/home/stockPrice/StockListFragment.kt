package com.ashomapp.presentation.home.stockPrice

import android.database.DataSetObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.AshomDBHelper
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentStockPriceBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.presentation.home.*
import com.ashomapp.presentation.home.stockPrice.adapter.StockPriceAdapter
import com.ashomapp.presentation.home.stockPrice.adapter.StockTitleAdapter
import com.ashomapp.presentation.home.stockPrice.model.StockListModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import com.ashomapp.presentation.news.NewsFrag
import com.ashomapp.utils.CenterScrollLayoutManager
import com.ashomapp.utils.SessionManager
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.notificationcounter
import com.bumptech.glide.Glide
import com.google.api.client.util.DateTime
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class StockListFragment : Fragment(R.layout.fragment_stock_price), onCountryClick,
    onCompanyClick, View.OnClickListener {


    private lateinit var countryList: List<CountriesDTO>
    private var draggingView: Int = -1
    private lateinit var countryAdapter: CountryAdapter
    private lateinit var stockFilteredData: List<StockListModelItem>
    private var mTouchedRvTag: Int = -1
    private lateinit var companiesList: List<CompanyDTO>
    private var countriesDTOData: CountriesDTO? = null
    private var countryCode: String = "KSA"
    private lateinit var stockList: StockListModel
    private lateinit var binding: FragmentStockPriceBinding

    private var currentView: View? = null

    private val mHomeViewModel by activityViewModels<HomeViewModel>()

    private val stockPriceAdapter = StockPriceAdapter()
    private val stockTitleAdapter = StockTitleAdapter()

    private lateinit var sessionManager: SessionManager
    var sortStock = false

    val db = AshomDBHelper(AshomAppApplication.instance.applicationContext, null)

    var stockListDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var stockListInitialized = false;
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (currentView == null) {
            currentView = inflater.inflate(R.layout.fragment_stock_price, container, false)
            binding = FragmentStockPriceBinding.bind(currentView!!)
            recyclerViewScrollHandling()
            init()
            setObserver()
            setAdapter()
            setOnClickListener()
        }
        return currentView!!
    }

    private fun recyclerViewScrollHandling() {
        val rvs = arrayListOf(binding.stockPricesTitleRv, binding.stockPricesRv)
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val currentRVTag = recyclerView.tag as Int

                if (currentRVTag == mTouchedRvTag) {
                    for (rvTag in 0 until rvs.size) {
                        if (rvTag != currentRVTag) {
                            rvs[rvTag].scrollBy(0, dy)
                        }
                    }
                }


            }
        }

        val itemTouchListener = object : OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val rvTag = rv.tag as Int

                if (mTouchedRvTag != -1 && mTouchedRvTag != rvTag) {
                    rvs[mTouchedRvTag].stopScroll()
                }

                mTouchedRvTag = rvTag

                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        }


        for (i in 0 until rvs.size) {
            val rv = rvs[i]
            rv.tag = i
            rv.addOnScrollListener(scrollListener)
            rv.addOnItemTouchListener(itemTouchListener)
        }


    }

    private fun setAdapter() {
        mHomeViewModel.getCountry()
        mHomeViewModel.countrylist.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.countryRv.layoutManager = CenterScrollLayoutManager(requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false)
                countryAdapter = CountryAdapter(it, this)

                binding.countryRv.adapter = countryAdapter
                countryList = it
                it.forEachIndexed { index, countriesDTO ->

                    if (countriesDTO.country == NewsFrag.country_name) {
                        Company_frag.selectedItem = index
                        countryAdapter.notifyDataSetChanged()
                        binding.countryRv.scrollToPosition(index)
                    }
                }
            }


            countryAdapter.setSubCountryShow(true)
            countryAdapter.setOnSubCountryClickListener { subCountry ->
                if (subCountry == "DFM") {
                    filterStockData(".DU")
                } else if (subCountry == "ADX") {
                    filterStockData(".AD")
                }
            }
        }
    }

    private fun init() {
        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                binding.toolbar.iconBadges.text = it.toString()
                binding.toolbar.iconBadges.visibility = View.VISIBLE
            } else {
                binding.toolbar.iconBadges.visibility = View.GONE
            }
        }
        HomeFlow.sectionBottomID = R.id.stockPriceFragment
        sessionManager = SessionManager(requireContext())
        Company_frag.selectedItem = 0
        drawableRightViewsUpArrow(binding.companyNameLbl)
        countryAdapter = CountryAdapter(emptyList(), this)
        println("first ${Calendar.getInstance().time}")
        mHomeViewModel.getStockLists(requireContext())
        mHomeViewModel.getCompany()

        binding.stockPriceHSV.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
            binding.topLabelScrollView.scrollTo(scrollX, scrollY)
        }

        binding.topLabelScrollView.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
            binding.stockPriceHSV.scrollTo(scrollX, scrollY)
        }

        try {

            if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load(SharedPrefrenceHelper.user.profile_pic).into(binding.toolbar.mainProfilePic)

            }
        }catch (e:Exception){
            println("Exception got.. $e")
        }

        println("got data ${sessionManager.stockListLocal.isEmpty()}  ${sessionManager.stockListLocal}")

        if (sessionManager.stockListLocal.isNotEmpty() && sessionManager.stockListLocal != "null") {

            val stockItem = Gson().fromJson(sessionManager.stockListLocal, StockListModel::class.java)
            binding.stockPricesRv.isVisible = true
            binding.stockPricesTitleRv.isVisible = true
            stockList = stockItem
            setStockList(stockList)
            stockFilteredData = stockList.filter { it.Identifier.contains(".SE") }
        }
    }


    private fun setObserver() {

        mHomeViewModel.stockCompaniesList.observe(viewLifecycleOwner) { it ->
            companiesList = it
            companiesList.onEach {
                var companyName = it.Company_Name.replace("_", "")
                companyName = companyName.replace("-", "")
                it.Company_Name = companyName.replace("  ", "")
            }
        }

        mHomeViewModel.stocksList.observe(viewLifecycleOwner) { response ->
            if (response.isEmpty()) {
                binding.stockPricesRv.isVisible = false
                binding.stockPricesTitleRv.isVisible = false
                Toast.makeText(requireContext(),
                    "Something went wrong. Please try again later.",
                    Toast.LENGTH_SHORT).show()
            } else {
                val stockJson = Gson().toJson(response)
                sessionManager.stockListLocal = stockJson
                binding.stockPricesRv.isVisible = true
                binding.stockPricesTitleRv.isVisible = true
                if (!stockListInitialized) {
                    stockList = response
                    stockFilteredData = stockList.filter { it.Identifier.contains(".SE") }
                    setStockList(stockFilteredData)
                }

            }
        }
    }


    private fun setStockList(response: List<StockListModelItem>) {
        stockListInitialized = true;
        stockTitleAdapter.differ.submitList(response.toMutableList())
        stockPriceAdapter.differ.submitList(response.toMutableList())
//        stockTitleAdapter.setStockList(response.toMutableList())

        stockPriceAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                binding.stockPricesRv.scrollToPosition(0)
                stockPriceAdapter.notifyDataSetChanged()
            }
        })

        stockTitleAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                binding.stockPricesTitleRv.scrollToPosition(0)
                stockTitleAdapter.notifyDataSetChanged()
            }

        })


        binding.stockPricesRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
            adapter = stockPriceAdapter

        }

        binding.stockPricesTitleRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
            adapter = stockTitleAdapter
        }


        ViewCompat.setNestedScrollingEnabled(binding.stockPricesTitleRv, false)
        ViewCompat.setNestedScrollingEnabled(binding.stockPricesRv, false)


    }

    private fun setOnClickListener() {


        with(binding) {
            companyNameLbl.setOnClickListener(this@StockListFragment)
            industryLbl.setOnClickListener(this@StockListFragment)
            lastLbl.setOnClickListener(this@StockListFragment)
            openLbl.setOnClickListener(this@StockListFragment)
            highLbl.setOnClickListener(this@StockListFragment)
            lowLbl.setOnClickListener(this@StockListFragment)
            closeLbl.setOnClickListener(this@StockListFragment)
            changeLbl.setOnClickListener(this@StockListFragment)
            changePercentLbl.setOnClickListener(this@StockListFragment)
            volumeLbl.setOnClickListener(this@StockListFragment)
            dateLbl.setOnClickListener(this@StockListFragment)
            exchangeLbl.setOnClickListener(this@StockListFragment)

            toolbar.icon.setOnClickListener(this@StockListFragment)
            toolbar.toolProfile.setOnClickListener(this@StockListFragment)
            toolbar.mainProfilePic.setOnClickListener(this@StockListFragment)
            toolbar.notificationBellIcon.setOnClickListener(this@StockListFragment)
        }
        binding.toolbar.mainBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.homeSearchIcon.setOnClickListener(this@StockListFragment)
        binding.companySort.setOnClickListener(this@StockListFragment)
        binding.companyNameLbl.setOnClickListener(this@StockListFragment)

        binding.search.doOnTextChanged { text, _, _, _ ->
            if (this::stockFilteredData.isInitialized) {
                if (text?.isEmpty()!!) {
                    hideKeyboard(requireContext(), binding.search)
                    binding.homeSearchIcon.setImageResource(R.drawable.search_icon)
                } else {
                    binding.homeSearchIcon.setImageResource(R.drawable.ic_close_search)
                }
                val stockFilteredData = stockFilteredData.filter {

                    it.Issuer_Name.lowercase()
                        .contains(text.toString().lowercase()) || it.RIC.lowercase()
                        .contains(text.toString().lowercase()) || it.SymbolTicker?.lowercase()
                        ?.contains(text.toString().lowercase()) == true
                }
                stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                //stockTitleAdapter.notifyDataSetChanged()
                //stockPriceAdapter.notifyDataSetChanged()
            }
        }

        stockPriceAdapter.setOnStockClickListener { stockData ->
            if (binding.search.isFocused && binding.search.text.isNullOrEmpty()) {
                hideKeyboard(AshomAppApplication.instance.applicationContext, binding.search)
                binding.search.clearFocus()
            } else {
                hideKeyboard(AshomAppApplication.instance.applicationContext, binding.search)
                MainActivity.financialtab = true
                binding.search.clearFocus()
                MainActivity.financiallistanimation = MainActivity.financiallistanimation + 1


                var referenceCompany = stockData.Reference_Company.replace("_", "")
                referenceCompany = referenceCompany.replace("-", "")
                referenceCompany = referenceCompany.replace("  ", "")
                referenceCompany = referenceCompany.replace(" ", "")


                var issuerName = stockData.Issuer_Name.replace("_", "")
                issuerName = issuerName.replace("-", "")
                issuerName = issuerName.replace("  ", "")
                issuerName = issuerName.replace(" ", "")
                issuerName = issuerName.substring(0, issuerName.length - 3)

                var stockSymbolTicker = stockData.RIC.replace("_", "")
                stockSymbolTicker = stockSymbolTicker.replace("-", "")
                stockSymbolTicker = stockSymbolTicker.replace("  ", "")
                stockSymbolTicker = stockSymbolTicker.replace(" ", "")
                stockSymbolTicker = stockSymbolTicker.substring(0, stockSymbolTicker.length - 3)

                var gotData = false
                var dbcompanyDTO: CompanyDTO? = null
                val companyListCountryWise =
                    companiesList.filter { it.Country.contains(countryCode) }


                companyListCountryWise.forEach {
                    var comapanyName = it.Company_Name
                    comapanyName = comapanyName.replace("-", "")
                    comapanyName = comapanyName.replace("  ", "")
                    comapanyName = comapanyName.replace(" ", "")
                    if (!gotData) {
                        if (stockSymbolTicker.lowercase() == it.SymbolTicker.lowercase() || referenceCompany.lowercase() == it.SymbolTicker.lowercase()) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (comapanyName.lowercase() == referenceCompany.lowercase() || comapanyName.lowercase()
                                .contains(referenceCompany.lowercase())
                        ) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (comapanyName.lowercase() == issuerName.lowercase() || comapanyName.lowercase()
                                .contains(issuerName.lowercase())
                        ) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (referenceCompany.lowercase()
                                .contains(comapanyName.lowercase())
                        ) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (issuerName.lowercase().contains(comapanyName.lowercase())) {
                            dbcompanyDTO = it
                            gotData = true
                        }
                    }
                }

                val bundle = Bundle()
                bundle.putString("countryCode", countryCode)
                val gson = Gson()
                val json = gson.toJson(stockData)


                val companyDTO = gson.toJson(dbcompanyDTO)

                val countryDTOJson = gson.toJson(countriesDTOData)
                bundle.putString("stockData", json)
                bundle.putString("countriesDTO", countryDTOJson)
                bundle.putString("flag", countryCode)
                bundle.putString("flag", countryCode)
                bundle.putString("companyDTO", companyDTO)
                if (dbcompanyDTO?.SymbolTicker == null) {
                    bundle.putString("symbolTicker", stockSymbolTicker.uppercase())
                } else {
                    bundle.putString("symbolTicker", dbcompanyDTO?.SymbolTicker)
                }
                bundle.putString("industry", dbcompanyDTO?.industry)
                findNavController().navigate(R.id.action_stockList_to_stockDetails, bundle)
            }
        }


        stockTitleAdapter.setOnStockClickListener { stockData ->
            if (binding.search.isFocused && binding.search.text.isNullOrEmpty()) {
                hideKeyboard(AshomAppApplication.instance.applicationContext, binding.search)
                binding.search.clearFocus()
            } else {
                hideKeyboard(AshomAppApplication.instance.applicationContext, binding.search)
                MainActivity.financialtab = true
                binding.search.clearFocus()
                MainActivity.financiallistanimation = MainActivity.financiallistanimation + 1


                var referenceCompany = stockData.Reference_Company.replace("_", "")
                referenceCompany = referenceCompany.replace("-", "")
                referenceCompany = referenceCompany.replace("  ", "")
                referenceCompany = referenceCompany.replace(" ", "")


                var issuerName = stockData.Issuer_Name.replace("_", "")
                issuerName = issuerName.replace("-", "")
                issuerName = issuerName.replace("  ", "")
                issuerName = issuerName.replace(" ", "")
                issuerName = issuerName.substring(0, issuerName.length - 3)

                var stockSymbolTicker = stockData.RIC.replace("_", "")
                stockSymbolTicker = stockSymbolTicker.replace("-", "")
                stockSymbolTicker = stockSymbolTicker.replace("  ", "")
                stockSymbolTicker = stockSymbolTicker.replace(" ", "")
                stockSymbolTicker = stockSymbolTicker.substring(0, stockSymbolTicker.length - 3)

                var gotData = false
                var dbcompanyDTO: CompanyDTO? = null
                val companyListCountryWise =
                    companiesList.filter { it.Country.contains(countryCode) }


                companyListCountryWise.forEach {
                    var comapanyName = it.Company_Name
                    comapanyName = comapanyName.replace("-", "")
                    comapanyName = comapanyName.replace(".", "")
                    comapanyName = comapanyName.replace("  ", "")
                    comapanyName = comapanyName.replace(" ", "")

                    if (!gotData) {
                        if (stockSymbolTicker.lowercase() == it.SymbolTicker.lowercase() || referenceCompany.lowercase() == it.SymbolTicker.lowercase()) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (comapanyName.lowercase() == referenceCompany.lowercase() || comapanyName.lowercase()
                                .contains(referenceCompany.lowercase())
                        ) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (comapanyName.lowercase() == issuerName.lowercase() || comapanyName.lowercase()
                                .contains(issuerName.lowercase())
                        ) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (referenceCompany.lowercase()
                                .contains(comapanyName.lowercase())
                        ) {
                            dbcompanyDTO = it
                            gotData = true
                        } else if (issuerName.lowercase().contains(comapanyName.lowercase())) {
                            dbcompanyDTO = it
                            gotData = true
                        }
                    }
                }

                val bundle = Bundle()
                bundle.putString("countryCode", countryCode)

                val gson = Gson()
                val json = gson.toJson(stockData)
                val companyDTO = gson.toJson(dbcompanyDTO)

                val countryDTOJson = gson.toJson(countriesDTOData)
                bundle.putString("stockData", json)
                bundle.putString("countriesDTO", countryDTOJson)
                bundle.putString("companyDTO", companyDTO)

                bundle.putString("flag", countryCode)
                bundle.putString("symbolTicker", dbcompanyDTO?.SymbolTicker)
                bundle.putString("industry", dbcompanyDTO?.industry)
                findNavController().navigate(R.id.action_stockList_to_stockDetails, bundle)
            }
        }
    }

    override fun onCompanyItemClick(companyDTO: CompanyDTO) {}


    override fun onCountryClick(countriesDTO: CountriesDTO, view: View, position: Int) {
        countriesDTOData = countriesDTO
        countryCode = countriesDTO.country
        drawableRightViewsUpArrow(binding.companyNameLbl)
        binding.countryRv.scrollToPosition(position)
        binding.stockPricesRv.isVisible = false
        binding.stockPricesTitleRv.isVisible = false

        when (countriesDTO.country) {
            "KSA" -> {
                filterStockData(".SE")
            }

            "UAE" -> {
                filterUAE()

            }

            "Oman" -> {
                filterStockData(".OM")
            }

            "Kuwait" -> {
                filterStockData(".KW")
            }

            "Bahrain" -> {
                filterStockData(".BH")
            }

            "Qatar" -> {
                filterStockData(".QA")
            }
        }

    }

    private fun filterUAE() {
        binding.companySort.setImageResource(R.drawable.ic_stock_arrow_up)

        if (this::stockList.isInitialized && stockList.isNotEmpty()) {
            binding.stockPricesRv.isVisible = true
            binding.stockPricesTitleRv.isVisible = true
            stockFilteredData =
                stockList.filter { it.Identifier.contains(".AD") || it.Identifier.contains(".DU") }

            stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
            stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
        } else {
            binding.stockPricesRv.isVisible = false
            binding.stockPricesTitleRv.isVisible = false
            Toast.makeText(requireContext(),
                "Something went wrong. Please try again later",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterStockData(countryCode: String) {
        binding.companySort.setImageResource(R.drawable.ic_stock_arrow_up)

        if (this::stockList.isInitialized && stockList.isNotEmpty()) {

            binding.stockPricesRv.isVisible = true
            binding.stockPricesTitleRv.isVisible = true
            stockFilteredData = stockList.filter { it.Identifier.contains(countryCode) }

            stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
            stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())

            binding.stockPricesTitleRv.scrollToPosition(0)
            binding.stockPricesRv.scrollToPosition(0)
            /*stockTitleAdapter.notifyDataSetChanged()
            stockPriceAdapter.notifyDataSetChanged()*/
            binding.stockPricesRv.isVisible = true
            binding.stockPricesTitleRv.isVisible = true
        } else {
            binding.stockPricesRv.isVisible = false
            binding.stockPricesTitleRv.isVisible = false
            Toast.makeText(requireContext(),
                "Something went wrong. Please try again later",
                Toast.LENGTH_SHORT).show()
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.home_search_icon -> {
                if (binding.search.text.isNotEmpty()) {
                    hideKeyboard(requireContext(), binding.search)
                    binding.search.setText("")
                }
            }

            R.id.companyNameLbl -> {


                sortStock = !sortStock
                try {
                    if (this::stockList.isInitialized || stockList.isNotEmpty()) {
                        if (sortStock) {
                            drawableRightViewsDownArrow(binding.companyNameLbl)
                            if (countryCode == "KSA") {
                                Collections.sort(stockFilteredData) { s1, s2 ->
                                    s2.Reference_Company.lowercase()
                                        .compareTo(s1.Reference_Company.lowercase())
                                }
                            } else {

                                Collections.sort(stockFilteredData) { s1, s2 ->
                                    "${s2.RIC.lowercase()}${s2.Reference_Company.lowercase()}"
                                        .compareTo("${s1.RIC.lowercase()}${s1.Reference_Company.lowercase()}")
                                }
                            }

                        } else {
                            if (countryCode == "KSA") {
                                Collections.sort(stockFilteredData) { s1, s2 ->
                                    s1.Reference_Company.lowercase()
                                        .compareTo(s2.Reference_Company.lowercase())
                                }
                            } else {
                                Collections.sort(stockFilteredData) { s1, s2 ->
                                    "${s1.RIC.lowercase()}${s1.Reference_Company.lowercase()}"
                                        .compareTo("${s2.RIC.lowercase()}${s2.Reference_Company.lowercase()}")
                                }
                            }
                            drawableRightViewsUpArrow(binding.companyNameLbl)
                        }

                        stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                        stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())


                    } else {
                        Toast.makeText(requireContext(),
                            "Something went wrong. Please try again later",
                            Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(),
                        "Something went wrong. Please try again later",
                        Toast.LENGTH_SHORT).show()
                }
            }


            R.id.industryLbl -> {

                sortStock = if (sortStock) {
                    drawableRightViewsDownArrow(binding.industryLbl)
                    !sortStock
                } else {
                    drawableRightViewsUpArrow(binding.industryLbl)
                    !sortStock
                }
            }
            R.id.lastLbl -> {
                sortStock = if (sortStock) {
                    binding.stockPricesRv.isVisible = false
                    binding.stockPricesTitleRv.isVisible = false
                    Collections.sort(stockFilteredData) { c1, c2 -> c2.Last.compareTo(c1.Last) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())

                    binding.stockPricesRv.isVisible = true
                    binding.stockPricesTitleRv.isVisible = true

                    binding.stockPricesRv.scrollToPosition(0)
                    binding.stockPricesTitleRv.scrollToPosition(0)

                    drawableRightViewsDownArrow(binding.lastLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 -> c1.Last.compareTo(c2.Last) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())

                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.lastLbl)
                    !sortStock

                }
            }
            R.id.openLbl -> {
                sortStock = if (sortStock) {

                    Collections.sort(stockFilteredData) { c1, c2 -> c2.Open.compareTo(c1.Open) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsDownArrow(binding.openLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 -> c1.Open.compareTo(c2.Open) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.openLbl)
                    !sortStock
                }
            }
            R.id.highLbl -> {
                sortStock = if (sortStock) {
                    Collections.sort(stockFilteredData) { c1, c2 -> c2.High.compareTo(c1.High) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()


                    drawableRightViewsDownArrow(binding.highLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 -> c1.High.compareTo(c2.High) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()
                    drawableRightViewsUpArrow(binding.highLbl)
                    !sortStock
                }
            }
            R.id.lowLbl -> {
                sortStock = if (sortStock) {
                    Collections.sort(stockFilteredData) { c1, c2 -> c2.Low.compareTo(c1.Low) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsDownArrow(binding.lowLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 -> c1.Low.compareTo(c2.Low) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.lowLbl)
                    !sortStock
                }
            }
            R.id.closeLbl -> {
                sortStock = if (sortStock) {
                    Collections.sort(stockFilteredData) { c1, c2 ->
                        c2.Universal_Close_Price.compareTo(c1.Universal_Close_Price)
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsDownArrow(binding.closeLbl)
                    !sortStock
                } else {

                    Collections.sort(stockFilteredData) { c1, c2 ->
                        c1.Universal_Close_Price.compareTo(c2.Universal_Close_Price)
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.closeLbl)
                    !sortStock
                }
            }
            R.id.changeLbl -> {
                sortStock = if (sortStock) {

                    Collections.sort(stockFilteredData) { c1, c2 -> c2.price_difference.compareTo(c1.price_difference) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsDownArrow(binding.changeLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 ->
                        c1.price_difference.toDouble().compareTo(c2.price_difference.toDouble())
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.changeLbl)
                    !sortStock
                }
            }
            R.id.changePercentLbl -> {
                sortStock = if (sortStock) {
                    Collections.sort(stockFilteredData) { c1, c2 ->
                        c2.percentage_change.toDouble().compareTo(c1.percentage_change.toDouble())
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()


                    drawableRightViewsDownArrow(binding.changePercentLbl)
                    !sortStock
                } else {

                    Collections.sort(stockFilteredData) { c1, c2 ->
                        c1.percentage_change.toDouble().compareTo(c2.percentage_change.toDouble())
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.changePercentLbl)
                    !sortStock

                }
            }
            R.id.volumeLbl -> {
                sortStock = if (sortStock) {
                    Collections.sort(stockFilteredData) { c1, c2 -> c2.Volume.compareTo(c1.Volume) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()


                    drawableRightViewsDownArrow(binding.volumeLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 -> c1.Volume.compareTo(c2.Volume) }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.volumeLbl)
                    !sortStock
                }
            }
            R.id.dateLbl -> {
                sortStock = if (sortStock) {
                    Collections.sort(stockFilteredData) { c1, c2 ->
                        stockListDateFormat.parse(c2.Trade_Date)
                            .compareTo(stockListDateFormat.parse(c1.Trade_Date))
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsDownArrow(binding.dateLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 ->
                        stockListDateFormat.parse(c1.Trade_Date)
                            .compareTo(stockListDateFormat.parse(c2.Trade_Date))
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.dateLbl)
                    !sortStock
                }
            }
            R.id.exchangeLbl -> {
                sortStock = if (sortStock) {
                    Collections.sort(stockFilteredData) { c1, c2 ->
                        c2.Exchange_Description.compareTo(c1.Exchange_Description)
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsDownArrow(binding.exchangeLbl)
                    !sortStock
                } else {
                    Collections.sort(stockFilteredData) { c1, c2 ->
                        c1.Exchange_Description.compareTo(c2.Exchange_Description)
                    }

                    stockTitleAdapter.differ.submitList(stockFilteredData.toMutableList())
                    stockPriceAdapter.differ.submitList(stockFilteredData.toMutableList())
                    //stockTitleAdapter.notifyDataSetChanged()
                    //stockPriceAdapter.notifyDataSetChanged()

                    drawableRightViewsUpArrow(binding.exchangeLbl)
                    !sortStock

                }
            }

            R.id.icon -> {
                (requireActivity() as MainActivity).gotoHome()
            }

            R.id.notification_bell_icon -> {
                hideKeyboard(AshomAppApplication.instance.applicationContext, binding.searchView)
                binding.searchView.clearFocus()
                findNavController().navigate(R.id.notificationFrag)
            }

            R.id.tool_profile -> {
//                hideKeyboard(AshomAppApplication.instance.applicationContext, binding.searchView)
//                binding.searchView.clearFocus()
                findNavController().navigate(R.id.settingFrag)
            }
            R.id.main_profile_pic -> {
//                hideKeyboard(AshomAppApplication.instance.applicationContext, binding.searchView)
//                binding.searchView.clearFocus()
                findNavController().navigate(R.id.settingFrag)
            }
        }
    }


    private fun sortStockList() {
        /*if indexPath.row == 0{
            selectedHeaderSorting = header[indexPath.column]
            if sortedColumn.column == indexPath.column {
                sortedColumn.sorting = sortedColumn.sorting == .ascending ? .descending : .ascending
            } else {
                sortedColumn = (indexPath.column, .ascending)
            }
            if selectedData.count > 0{
                if indexPath.column == header.count - 2{
                    selectedData.sort {
                        let ascending = tradedateFormatter.date(from: $0[sortedColumn.column])! < tradedateFormatter.date(from: $1[sortedColumn.column])!
                        return sortedColumn.sorting == .ascending ? ascending : !ascending
                    }
                }else if indexPath.column  == 0{
                    selectedData.sort {
                        let ascending = String($0[sortedColumn.column].split(separator: "@")[1]) < String($1[sortedColumn.column].split(separator: "@")[1])
                        return sortedColumn.sorting == .ascending ? ascending : !ascending
                    }
                }else if indexPath.column  == header.count - 1{
                    selectedData.sort {
                        let ascending = ($0[sortedColumn.column]) < ($1[sortedColumn.column])
                        return sortedColumn.sorting == .ascending ? ascending : !ascending
                    }
                }else{
                    selectedData.sort {
                        let ascending = Double($0[sortedColumn.column].replacingOccurrences(of: "+", with: "").replacingOccurrences(of: "", with: "").replacingOccurrences(of: "%", with: "").replacingOccurrences(of: "B", with: "").replacingOccurrences(of: "b", with: "").replacingOccurrences(of: ",", with: ""))! < Double($1[sortedColumn.column].replacingOccurrences(of: "", with: "").replacingOccurrences(of: "", with: "").replacingOccurrences(of: "%", with: "").replacingOccurrences(of: "B", with: "").replacingOccurrences(of: "b", with: "").replacingOccurrences(of: "", with: "").replacingOccurrences(of: "", with: "").replacingOccurrences(of: "%", with: "").replacingOccurrences(of: "B", with: "").replacingOccurrences(of: "b", with: "").replacingOccurrences(of: ",", with: ""))!
                        return sortedColumn.sorting == .ascending ? ascending : !ascending
                    }
                }

                spreadsheetView.reloadData()
                return
            }
            return
        }*/
    }

    private fun drawableRightViewsUpArrow(viewLbl: TextView) {
        binding.companyNameLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.industryLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.lastLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.openLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.highLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.lowLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.closeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.changeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.changePercentLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.volumeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.dateLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.exchangeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        viewLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up_stock_list, 0)
    }

    private fun drawableRightViewsDownArrow(viewLbl: TextView) {
        binding.companyNameLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.industryLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.lastLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.openLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.highLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.lowLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.closeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.changeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.changePercentLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.volumeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.dateLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.exchangeLbl.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        viewLbl.setCompoundDrawablesWithIntrinsicBounds(0,
            0,
            R.drawable.ic_arrow_down_stock_list,
            0)

    }

    override fun onResume() {
        if (!this::stockFilteredData.isInitialized) {
            init()
        }
        setObserver()
        super.onResume()
    }
}
