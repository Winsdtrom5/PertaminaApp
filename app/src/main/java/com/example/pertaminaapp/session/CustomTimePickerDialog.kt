package com.example.pertaminaapp.session

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class CustomTimePickerDialog : DialogFragment() {

    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    private var isNormalPola: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): TimePickerDialog {
        val calendar = Calendar.getInstance()
        var hour: Int
        var minute: Int

        if (isNormalPola) {
            // Set the initial time to 18:00 if it's a normal "pola"
            hour = 18
            minute = 0
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)

            // Ensure the time is not below 06:00
            if (hour < 6) {
                hour = 6
                minute = 0
            }
        }

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                timeSetListener.onTimeSet(null, selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        )

        return timePickerDialog
    }

    fun setTimeSetListener(listener: TimePickerDialog.OnTimeSetListener) {
        timeSetListener = listener
    }

    fun setIsNormalPola(normalPola: Boolean) {
        isNormalPola = normalPola
    }
}
