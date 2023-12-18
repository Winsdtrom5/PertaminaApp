package com.example.pertaminaapp.session
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class CustomXAxisValueFormatter(private val labels: List<String>) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return if (value >= 0 && value < labels.size) {
            labels[value.toInt()]
        } else {
            ""
        }
    }
}
