package com.example.pertaminaapp

import android.R
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.databinding.ActivityLaporanBinding
import com.example.pertaminaapp.model.SpklData
import com.example.pertaminaapp.session.CustomXAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.text.DecimalFormat
import java.text.NumberFormat

class LaporanActivity : AppCompatActivity() {
    private lateinit var status: AutoCompleteTextView
    private lateinit var bulan: AutoCompleteTextView
    private lateinit var tahun: AutoCompleteTextView
    private val spklDataList = mutableListOf<SpklData>()
    private lateinit var binding: ActivityLaporanBinding
    private lateinit var generate : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        status = binding.acstatus
        bulan = binding.acbulan
        tahun = binding.actahun
        generate = binding.button
        // Set up text change listeners for AutoCompleteTextViews
        status.addTextChangedListener(statusWatcher)
        bulan.addTextChangedListener(bulanWatcher)
        tahun.addTextChangedListener(tahunWatcher)
        generate.setOnClickListener{
            if(status.toString().isNotBlank() && bulan.toString().isNotBlank() && tahun.toString().isNotBlank()){
                makeGraph(status.toString(),bulan.toString(),tahun.toString())
            }
        }
    }
    private fun createTextWatcher(
        autoCompleteTextView: AutoCompleteTextView,
        onChanged: (String) -> Unit
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChanged(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed
            }
        }
    }
    private val statusWatcher = createTextWatcher(status) {
        // Clear bulan and tahun when status changes
        bulan.text.clear()
        tahun.text.clear()

        // Populate bulan based on the new status
        populateBulanAutoComplete(it)
    }

    // TextWatcher for bulan
    private val bulanWatcher = createTextWatcher(bulan) {
        // Clear tahun when bulan changes
        tahun.text.clear()

        // Populate tahun based on the new status and bulan
        populateTahunAutoComplete(status.text.toString(), it)
    }

    // TextWatcher for tahun
    private val tahunWatcher = createTextWatcher(tahun) {
        // Enable or disable the generate button based on whether tahun is selected
        generate.isEnabled = true
    }
    private fun makeGraph(status : String, bulan: String, tahun: String){
        GlobalScope.launch(Dispatchers.IO) {
            // Check the username and password in the database
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query =
                        """SELECT c.nama_unit_org, SUM(a.uang_lembur) AS total_uang_lembur
                        FROM spkl AS a
                        JOIN biodata AS b ON a.kode_pekerja = b.kode_pekerja
                        JOIN master_unit AS c ON b.cost_center_pengguna = c.cost_center_pengguna
                        GROUP BY c.nama_unit_org where a.status = ? AND MONTH(tanggal) = ?, Year(tanggal) = ?""".trimIndent()
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, status)
                    preparedStatement.setString(2, bulan)
                    preparedStatement.setString(3, tahun)
                    val resultSet: ResultSet = preparedStatement.executeQuery()
                    val entries = mutableListOf<BarEntry>()
                    val legendLabels = mutableListOf<String>()
                    while (resultSet.next()) {
                        val namaUnit = resultSet.getString("nama_unit_org")
                        val totalUangLembur = resultSet.getFloat("total_uang_lembur")

                        // Add data to the entries list
                        entries.add(BarEntry(entries.size.toFloat(), totalUangLembur))

                        // Add unit name to legend labels
                        legendLabels.add(namaUnit)
                    }

                    // Create and show the chart on the UI thread
                    runOnUiThread {
                        showBarChart(entries, legendLabels)
                    }
                } catch (e: SQLException) {
                    runOnUiThread {
                        Toast.makeText(this@LaporanActivity, "Error Has Occured", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showBarChart(entries: List<BarEntry>, legendLabels: List<String>) {
        val dataSet = BarDataSet(entries, "Total Uang Lembur")
        dataSet.colors = getColors(entries.size)

        val data = BarData(dataSet)

        val barChart = BarChart(this)
        barChart.data = data
        barChart.setFitBars(true)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = CustomXAxisValueFormatter(legendLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        val yAxis = barChart.axisLeft
        yAxis.valueFormatter = MoneyValueFormatter()

        barChart.description.isEnabled = false

        val legend = barChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        barChart.invalidate()

        // Now, you can add barChart to your layout where you want to display it.
        // For example, you can add it to a LinearLayout in your XML layout file.
    }

    private fun getColors(size: Int): List<Int> {
        // Generate random colors or use a predefined list based on your requirement
        val colors = mutableListOf<Int>()
        for (i in 0 until size) {
            colors.add(Color.rgb((0..255).random(), (0..255).random(), (0..255).random()))
        }
        return colors
    }

    private class MoneyValueFormatter : ValueFormatter() {
        private val currencyFormat: NumberFormat = DecimalFormat("###,###,###,###")

        override fun getFormattedValue(value: Float): String {
            return "Rp ${currencyFormat.format(value)}"
        }
    }
    private fun populateStatusAutoComplete() {
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            getStatusList()
        )
        status.setAdapter(statusAdapter)
//
//        status.setOnItemClickListener { _, _, _, _ ->
//            populateBulanAutoComplete(status.text.toString())
//        }

        // Disable and show toast if status is not selected
        if (status.text.isBlank()) {
            disableAutoComplete(bulan)
            disableAutoComplete(tahun)
            showToast("Please select Status")
        }
    }

    private fun populateBulanAutoComplete(selectedStatus: String) {
        val bulanAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            getBulanList(selectedStatus)
        )
        bulan.setAdapter(bulanAdapter)
//
//        bulan.setOnItemClickListener { _, _, _, _ ->
//            populateTahunAutoComplete(selectedStatus, bulan.text.toString())
//        }

        // Disable and show toast if bulan is not selected
        if (status.text.isBlank()) {
            disableAutoComplete(tahun)
            showToast("Please select Bulan")
        }
    }

    private fun populateTahunAutoComplete(selectedStatus: String, selectedBulan: String) {
        val tahunAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            getTahunList(selectedStatus, selectedBulan)
        )
        tahun.setAdapter(tahunAdapter)

        // Disable and show toast if tahun is not selected
        if (bulan.text.isBlank()) {
            showToast("Please select Tahun")
        }
    }

    private fun getStatusList(): List<String> {
        return spklDataList.map { it.posisi }.distinct()
    }

    private fun getBulanList(selectedStatus: String): List<String> {
        return spklDataList.filter { it.posisi == selectedStatus }
            .map { it.bulan.toString() }
            .distinct()
    }

    private fun getTahunList(selectedStatus: String, selectedBulan: String): List<String> {
        return spklDataList.filter { it.posisi == selectedStatus && it.bulan.toString() == selectedBulan }
            .map { it.tahun.toString() }
            .distinct()
    }
    private fun disableAutoComplete(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.setText("")
        autoCompleteTextView.isEnabled = false
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun getData(){
        GlobalScope.launch(Dispatchers.IO) {
            // Check the username and password in the database
            val connection = eworks.getConnection()
            if (connection != null) {
                try {
                    val query =
                        """SELECT MONTH(tanggal) As Bulan, Year(tanggal) as Tahun, posisi from spkl""".trimIndent()
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    val resultSet: ResultSet = preparedStatement.executeQuery()
                    while (resultSet.next()) {
                        val bulan = resultSet.getInt("Bulan")
                        val tahun = resultSet.getInt("Tahun")
                        val posisi = resultSet.getString("posisi")

                        val spklData = SpklData(bulan, tahun, posisi)
                        spklDataList.add(spklData)
                    }
                    runOnUiThread {
                        populateStatusAutoComplete()
                    }
                }catch (e: SQLException) {
                    runOnUiThread {
                        Toast.makeText(this@LaporanActivity, "Error Has Occured", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}