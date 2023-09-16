package com.example.pertaminaapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pertaminaapp.R
import com.example.pertaminaapp.adapter.DinasAdapter
import com.example.pertaminaapp.adapter.LemburAdapter
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.model.DinasItem
import com.example.pertaminaapp.model.LemburItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Locale

class DaftarDinasFragment : Fragment(), FilterDialogFragment.FilterDialogListener   {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DinasAdapter
    private val dinasList: MutableList<DinasItem> = mutableListOf()
    private var kode: String? = null
    private lateinit var searchView:SearchView
    private lateinit var textDataNotFound: TextView
    private var lastSelectedStatus: String? = null
    private var lastSelectedKota: String? = null
    private var lastSelectedTahun: String? = null
    private var isFilterApplied = false
    private var selectedStatus: String? = null
    private var selectedKota: String? = null
    private var selectedTahun: String? = null
    private var filteredDinasList: List<DinasItem> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_daftar_dinas, container, false)
        kode = arguments?.getString("kode")
        recyclerView = view.findViewById(R.id.rv1)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        textDataNotFound = view.findViewById(R.id.text_data_not_found)
        val kotaDataList = readAndParseKotaJson()
        val provinsiDataList = readAndParseProvinsiJson()
        // Combine and display the data
        val combinedDataList = combineAndFormatData(kotaDataList, provinsiDataList)
        // Initialize the adapter with an empty list for now
        val filterIcon = view.findViewById<ImageView>(R.id.filter_icon)
        filterIcon.setOnClickListener {
            // Show the filter dialog when the icon is clicked
            showFilterDialog()
        }
        searchView = view.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredData = if (isFilterApplied) {
                    // Search in filteredLemburList
                    filteredDinasList.filter { item ->
                        item.pekerjaan.toLowerCase(Locale.getDefault()).contains(newText.orEmpty().toLowerCase(Locale.getDefault()))
                    }
                } else {
                    // Search in lemburList
                    dinasList.filter { item ->
                        item.pekerjaan.toLowerCase(Locale.getDefault()).contains(newText.orEmpty().toLowerCase(Locale.getDefault()))
                    }
                }

                // Update the adapter with the filtered data
                adapter.updateFilter(filteredData)

                // Show or hide textDataNotFound based on whether data is found
                textDataNotFound.visibility = if (filteredData.isEmpty()) View.VISIBLE else View.GONE

                return true
            }
        })
        adapter = DinasAdapter(dinasList)
        recyclerView.adapter = adapter

        // Fetch data from MySQL
        fetchDataFromMySQL()
        return view
    }
    private fun showFilterDialog() {
        val fragmentManager = childFragmentManager

        val (uniqueBulanList, uniqueTahunList) = getBulanAndTahunLists()

        // Assuming you have access to the `kotaList` and `dinasList` variables
        val kotaList = readAndParseKotaJson()
        val provinsiList = readAndParseProvinsiJson()

        val combinedDataMap = combineAndFormatData(kotaList, provinsiList)

        val autoCompleteKota = combinedDataMap.keys.toTypedArray()

        val filterDinasFragment = FilterDinasFragment.newInstance(
            autoCompleteKota.toList(), // Pass the user-entered city names
            uniqueTahunList,
            lastSelectedKota,
            lastSelectedTahun,
            lastSelectedStatus
        )

        filterDinasFragment.show(fragmentManager, "com.example.pertaminaapp.fragment.FilterDinasFragment")
    }


    private fun readAndParseKotaJson(): List<JSONObject> {
        val kotaDataList = mutableListOf<JSONObject>()
        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.kota)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charsets.UTF_8)

            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                kotaDataList.add(jsonObject)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return kotaDataList
    }

    private fun readAndParseProvinsiJson(): List<JSONObject> {
        val provinsiDataList = mutableListOf<JSONObject>()
        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.provinsi)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charsets.UTF_8)

            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                provinsiDataList.add(jsonObject)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return provinsiDataList
    }

    private fun getBulanAndTahunLists(): Pair<List<String>, List<String>> {
        val bulanList = ArrayList<String>()
        val tahunList = ArrayList<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (dinasItem in dinasList) {
            try {
                val tanggalbrangkat = dateFormat.parse(dinasItem.tanggalberangkat)
                val tanggalpulang = dateFormat.parse(dinasItem.tanggalpulang)
                val bulanNumber = SimpleDateFormat("MM", Locale.getDefault()).format(tanggalbrangkat)
                val bulanNumber2 = SimpleDateFormat("MM", Locale.getDefault()).format(tanggalpulang)
                val tahunberangakt = SimpleDateFormat("yyyy", Locale.getDefault()).format(tanggalbrangkat)
                val tahunpulang = SimpleDateFormat("yyyy", Locale.getDefault()).format(tanggalpulang)

                if (!bulanList.contains(bulanNumber) && !bulanList.contains(bulanNumber2)) {
                    bulanList.add(bulanNumber)
                    bulanList.add(bulanNumber2)
                }
                if (!tahunList.contains(tahunberangakt) && !tahunList.contains(tahunpulang)) {
                    tahunList.add(tahunberangakt)
                    tahunList.add(tahunpulang)
                }
            } catch (e: Exception) {
                Log.e("Error", "Error parsing date: ${dinasItem.tanggalberangkat}")
            }
        }

        // Convert bulan numbers to Indonesian month names and sort them
        val indonesianMonthNames = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val sortedBulanList = bulanList.sortedBy { it.toInt() }
            .map { indonesianMonthNames[it.toInt() - 1] }

        return Pair(sortedBulanList, tahunList)
    }

    private fun combineAndFormatData(
        kotaList: List<JSONObject>,
        provinsiList: List<JSONObject>
    ): Map<String, String> {
        val combinedDataMap = mutableMapOf<String, String>()

        for (dinasItem in dinasList) {
            try {
                val kotaNamesString = dinasItem.tujuan
                // Check if the kotaNamesString contains commas
                if (kotaNamesString.contains(",")) {
                    // Split the string by commas to get individual city names
                    val kotaNames = kotaNamesString.split(",")
                    // Iterate through the list of city names
                    for (kotaName in kotaNames) {
                        val cleanedKotaName = kotaName.trim().lowercase(Locale.getDefault())
                        val matchingProvinsis = provinsiList.filter {
                            it.optString("Nama_Daerah").lowercase(Locale.getDefault())
                                .contains(cleanedKotaName)
                        }
                        if (matchingProvinsis.isNotEmpty()) {
                            // Add all matching provinsi names with the user-entered city name
                            matchingProvinsis.forEach { provinsi ->
                                combinedDataMap[cleanedKotaName] = provinsi.optString("Kota")
                            }
                        }
                    }
                } else {
                    val cleanedKotaName = kotaNamesString.trim().toLowerCase(Locale.getDefault())
                    val matchingProvinsis = provinsiList.filter {
                        it.optString("Nama_Daerah").toLowerCase(Locale.getDefault())
                            .contains(cleanedKotaName)
                    }
                    if (matchingProvinsis.isNotEmpty()) {
                        // Add all matching provinsi names with the user-entered city name
                        matchingProvinsis.forEach { provinsi ->
                            combinedDataMap[cleanedKotaName] = provinsi.optString("Kota")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Error", "Error parsing city name: $e")
            }
        }

        return combinedDataMap
    }


    private fun fetchDataFromMySQL() {
        setLoading(true)
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            try {
                val connection = eworks.getConnection()
                if (connection != null) {
                    val query = "SELECT * FROM spd WHERE kode_pekerja = ?"
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, kode)
                    val resultSet: ResultSet = preparedStatement.executeQuery()

                    // Create a temporary list to store the data
                    val tempList: MutableList<DinasItem> = mutableListOf()

                    while (resultSet.next()) {
                        val pekerjaan = resultSet.getString("keterangan")
                        Log.d("A", pekerjaan)
                        val mulai = resultSet.getString("mulai")
                        val akhir = resultSet.getString("akhir")
                        val tujuan = resultSet.getString("tujuan")
                        val status = resultSet.getString("status")
                        val dinasItem = DinasItem(pekerjaan, tujuan, mulai, akhir, status)

                        // Add each item to the temporary list
                        tempList.add(dinasItem)
                    }

                    // Update the UI on the main thread with the complete list
                    withContext(Dispatchers.Main) {
                        // Clear the existing list and add all items from tempList
                        dinasList.clear()
                        dinasList.addAll(tempList)
                        adapter.notifyDataSetChanged()
                        if (dinasList.isEmpty()) {
                            textDataNotFound.visibility = View.VISIBLE
                        } else {
                            textDataNotFound.visibility = View.GONE
                        }
                        setLoading(false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Tidak dapat tersambung", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        val loadingLayout = activity?.findViewById<LinearLayout>(R.id.layout_loading)
        if (isLoading) {
            loadingLayout?.visibility = View.VISIBLE
        } else {
            loadingLayout?.visibility = View.INVISIBLE
        }
    }

    override fun onFilterApplied(selectedStatus: String, selectedKota: String, selectedTahun: String) {
        this.selectedStatus = if (selectedStatus.isBlank()) null else selectedStatus
        this.selectedKota = if (selectedKota.isBlank()) null else selectedKota
        this.selectedTahun = if (selectedTahun.isBlank()) null else selectedTahun

        // Apply the filters to the originalLemburList
        applyFilters()
        isFilterApplied = true // Filters are applied

        // Save the last selected filter values
        lastSelectedStatus = selectedStatus
        lastSelectedKota = selectedKota
        lastSelectedTahun = selectedTahun
    }

    private fun applyFilters() {
        // Filter your data based on the selected filters
        filteredDinasList = dinasList.filter { item ->
            (selectedStatus == null || item.status == selectedStatus) &&
//                    (selectedKota == null || extractMonth(selectedBulan!!) == extractBulan(item.tanggal)) &&
                    (selectedTahun == null || selectedTahun == extractTahun(item.tanggalberangkat))
        }
        if (filteredDinasList.isEmpty()) {
            adapter.updateFilter(emptyList())
            textDataNotFound.visibility = View.VISIBLE
        } else {
            textDataNotFound.visibility = View.GONE
            // Update the adapter with the filtered list
            adapter.updateFilter(filteredDinasList)
        }
    }

    private fun extractTahun(date: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(date)
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(parsedDate)
    }
}