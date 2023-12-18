import android.content.Context
import android.widget.DatePicker
import android.app.DatePickerDialog
import com.example.pertaminaapp.model.HolidayList

class CustomDatePickerDialog(
    context: Context,
    onDateSetListener: OnDateSetListener?,
    private val holidayList: HolidayList
) : DatePickerDialog(context, onDateSetListener, 0, 0, 0) {

    override fun onDateChanged(view: DatePicker, year: Int, month: Int, day: Int) {
        super.onDateChanged(view, year, month, day)
        val selectedDate = "$year-${month + 1}-$day"
        if (!isDateValid(selectedDate)) {
            view.updateDate(year, month, day)
        }
    }

    private fun isDateValid(selectedDate: String): Boolean {
        return isDateInHolidayList(selectedDate)
    }

    private fun isDateInHolidayList(date: String): Boolean {
        return holidayList.holidayList.any { it.tanggal == date }
    }
}
