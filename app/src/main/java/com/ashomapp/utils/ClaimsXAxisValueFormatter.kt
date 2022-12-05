package com.ashomapp.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


class ClaimsXAxisValueFormatter(private val datesList: List<Date>) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val position = value.toInt()
        println("hello $value")
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormat.timeZone = TimeZone.getTimeZone("GMT+4")

        if (position % 2 == 0) {
                return timeFormat.format(datesList[position] )
        }
        return ""
    }
}

class ClaimsXAxisValueFormatterOneWeek(private val datesList: List<Date>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val position = value.toInt()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        if (position  == 0) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 10) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 20) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }
        return ""
    }
}

class ClaimsXAxisValueFormatterOneMonth(private val datesList: List<Date>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val position = value.toInt()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        if (position  == 0) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 30) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 60) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 90) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 120) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }

        return ""
    }
}

class ClaimsXAxisValueFormatterSixMonth(private val datesList: List<Date>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val position = value.toInt()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        println("==> 6 month ${datesList.size}  $position")
        if (position  == 0) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 30) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 60) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 90) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }
        return ""
    }
}

class ClaimsXAxisValueFormatterOneYear(private val datesList: List<Date>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val position = value.toInt()
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        if (position  == 0) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 100) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 200) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }

        return ""
    }
}

class ClaimsXAxisValueFormatterFiveYear(private val datesList: List<Date>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val position = value.toInt()
        val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        if (position  == 0) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 300) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 600) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }else if (position  == 900) {
            if (position < datesList.size) {
                return dateFormat.format(datesList[position])
            }
        }
        return ""
    }
}