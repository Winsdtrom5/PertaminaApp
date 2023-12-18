package com.example.pertaminaapp.session
import android.content.Context
import android.view.ViewGroup
import android.widget.DatePicker
import com.example.pertaminaapp.model.HolidayList
import java.util.Calendar

class CustomDatePicker(
    context: Context,
    private val holidayList: HolidayList
) : DatePicker(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH)
        val day = today.get(Calendar.DAY_OF_MONTH)

        updateDate(year, month, day)

        // Use holidayList to disable specific dates
        if (holidayList.holidayList.isNotEmpty()) {
            for (holiday in holidayList.holidayList) {
                val holidayDate = holiday.tanggal // Adjust this line to get the date in the desired format

                if (isDateMatch(holidayDate, year)) {
                    val day = holidayDate.split("-")[2].toInt()
                    val dayIndex = day - 1
                    val view = getChildAt(0)
                    if (view is ViewGroup) {
                        val child = view.getChildAt(dayIndex)
                        child.isEnabled = false
                        child.alpha = 0.5f
                    }
                }
            }
        }
    }

    private fun isDateMatch(holidayDate: String, year: Int): Boolean {
        val dateParts = holidayDate.split("-") // Split the date string by '-'
        if (dateParts.size >= 3) {
            val holidayYear = dateParts[0].toInt() // Extract the year part as an integer
            return holidayYear == year // Compare the extracted year with the current year
        }
        return false // Invalid date format
    }
}
