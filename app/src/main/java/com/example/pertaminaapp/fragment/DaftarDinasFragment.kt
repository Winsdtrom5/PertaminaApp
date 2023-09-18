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

class DaftarDinasFragment : Fragment(), FilterDinasFragment.FilterDialogListener   {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DinasAdapter
    private val dinasList: MutableList<DinasItem> = mutableListOf()
    private var kode: String? = null
    private lateinit var searchView:SearchView
    private lateinit var textDataNotFound: TextView
    private var lastSelectedStatus: String? = null
    private var lastSelectedKota: String? = null
    private var lastSelectedTahun: String? = null
    private var lastSelectedBulan: String? = null
    private var isFilterApplied = false
    private var selectedStatus: String? = null
    private var selectedKota: String? = null
    private var selectedBulan: String? = null
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
            autoCompleteKota.toList(),
            uniqueBulanList,
            uniqueTahunList,
            lastSelectedKota,
            lastSelectedTahun,
            lastSelectedStatus,
            lastSelectedBulan
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
        val bulanSet = HashSet<String>()
        val tahunSet = HashSet<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val indonesianMonthNames = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        for (dinasItem in dinasList) {
            try {
                val tanggalberangkat = dateFormat.parse(dinasItem.tanggalberangkat)
                val tanggalpulang = dateFormat.parse(dinasItem.tanggalpulang)
                val bulanNumber = SimpleDateFormat("MM", Locale.getDefault()).format(tanggalberangkat)
                val bulanNumber2 = SimpleDateFormat("MM", Locale.getDefault()).format(tanggalpulang)
                val tahunberangkat = SimpleDateFormat("yyyy", Locale.getDefault()).format(tanggalberangkat)
                val tahunpulang = SimpleDateFormat("yyyy", Locale.getDefault()).format(tanggalpulang)

                if (!bulanSet.contains(bulanNumber)) {
                    bulanSet.add(bulanNumber)
                }

                if (!bulanSet.contains(bulanNumber2) && bulanNumber != bulanNumber2) {
                    bulanSet.add(bulanNumber2)
                }

                if (!tahunSet.contains(tahunberangkat)) {
                    tahunSet.add(tahunberangkat)
                }

                if (!tahunSet.contains(tahunpulang) && tahunberangkat != tahunpulang) {
                    tahunSet.add(tahunpulang)
                }

            } catch (e: Exception) {
                Log.e("Error", "Error parsing date: ${dinasItem.tanggalberangkat}")
            }
        }
        val sortedBulanList = bulanSet
            .toList()
            .sortedBy { it.toInt() }
            .map { indonesianMonthNames[it.toInt() - 1] }
        return Pair(sortedBulanList, tahunSet.toList())
    }


    private fun extractMonth(date: String): String {
        val monthNameToNumber = mapOf(
            "Januari" to "01",
            "Februari" to "02",
            "Maret" to "03",
            "April" to "04",
            "Mei" to "05",
            "Juni" to "06",
            "Juli" to "07",
            "Agustus" to "08",
            "September" to "09",
            "Oktober" to "10",
            "November" to "11",
            "Desember" to "12"
        )

        // Check if the provided month name exists in the mapping
        val monthNumber = monthNameToNumber[date]

        // Use the mapping to convert the month name to 2-digit format
        return monthNumber ?: "00"
    }

    private fun combineAndFormatData(
        kotaList: List<JSONObject>,
        provinsiList: List<JSONObject>
    ): Map<String, String> {
        val combinedDataMap = mutableMapOf<String, String>()
        val idKotaMapping = mutableMapOf<Int, Int>()
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
                            it.optString("Kota").lowercase(Locale.getDefault())
                                .contains(cleanedKotaName)
                        }
                        if (matchingProvinsis.isNotEmpty()) {
                            // Add all matching provinsi names with the user-entered city name
                            matchingProvinsis.forEach { provinsi ->
                                val provinsi2 = matchingProvinsis.first()
                                val provinsiName = provinsi2.optString("Kota")
                                combinedDataMap[provinsiName] = provinsiName
                            }
                        }else{
                            val matchingKotas = kotaList.filter {
                                it.optString("Nama_Daerah").lowercase(Locale.getDefault())
                                    .contains(cleanedKotaName)
                            }
                            matchingKotas.forEach { kotaItem ->
                                val idKota = kotaItem.optInt("ID_Kota")
                                val idProvinsi = idKotaMapping[idKota]
                                if (idProvinsi != null) {
                                    val provinsiObject = provinsiList.find {
                                        it.optInt("Id") == idProvinsi
                                    }
                                    val provinsiName = provinsiObject?.optString("Kota") ?: ""
                                    val kota = "${provinsiObject?.optString("Nama_Daerah")} - $provinsiName"
                                    combinedDataMap[kota] = kota
                                }
                            }
                        }
                    }
                } else {
                    val cleanedKotaName = kotaNamesString.trim().toLowerCase(Locale.getDefault())
                    val matchingProvinsis = provinsiList.filter {
                        it.optString("Kota").toLowerCase(Locale.getDefault())
                            .contains(cleanedKotaName)
                    }
                    if (matchingProvinsis.isNotEmpty()) {
                        // Add all matching provinsi names with the user-entered city name
                        matchingProvinsis.forEach { provinsi ->
                            val provinsi2 = matchingProvinsis.first()
                            val provinsiName = provinsi2.optString("Kota")
                            combinedDataMap[provinsiName] = provinsiName
                        }
                    }else{
                        val matchingKotas = kotaList.filter {
                            it.optString("Nama_Daerah").lowercase(Locale.getDefault())
                                .contains(cleanedKotaName)
                        }
                        matchingKotas.forEach { kotaItem ->
                            val idKota = kotaItem.optInt("ID_Kota")
                            val idProvinsi = idKotaMapping[idKota]
                            if (idProvinsi != null) {
                                val provinsiObject = provinsiList.find {
                                    it.optInt("ID_Provinsi") == idProvinsi
                                }
                                val provinsiName = provinsiObject?.optString("Kota") ?: ""
                                val kota = "${provinsiObject?.optString("Nama_Daerah")} - $provinsiName"
                                combinedDataMap[kota] = kota
                            }
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

    override fun onFilterApplied(selectedStatus: String, selectedKota: String, selectedTahun: String,selectedBulan : String) {
        this.selectedStatus = if (selectedStatus.isBlank()) null else selectedStatus
        this.selectedKota = if (selectedKota.isBlank()) null else selectedKota
        this.selectedTahun = if (selectedTahun.isBlank()) null else selectedTahun
        this.selectedBulan = if (selectedBulan.isBlank()) null else selectedBulan
        // Apply the filters to the originalLemburList
        applyFilters()
        isFilterApplied = true // Filters are applied

        // Save the last selected filter values
        lastSelectedStatus = selectedStatus
        lastSelectedKota = selectedKota
        lastSelectedTahun = selectedTahun
        lastSelectedBulan = selectedBulan
    }
    private fun extractBulan(date: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(date)
        return SimpleDateFormat("MM", Locale.getDefault()).format(parsedDate)
    }
    private fun applyFilters() {
        val kotaAutocompleteMap = createAutocompleteMap()
        filteredDinasList = dinasList.filter { item ->
            selectedStatus == null || item.status == selectedStatus
            selectedBulan == null ||
                    (extractMonth(selectedBulan!!) == extractBulan(item.tanggalberangkat) || extractMonth(selectedBulan!!) == extractBulan(item.tanggalpulang))
            selectedTahun == null ||
                    (selectedTahun == extractTahun(item.tanggalberangkat) || selectedTahun == extractTahun(item.tanggalpulang))
            selectedKota == null ||
                    (kotaAutocompleteMap[item.tujuan]?.equals(selectedKota, ignoreCase = true) == true)

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

    private fun createAutocompleteMap(): Map<String, String> {
        val autocompleteMap = mutableMapOf<String, String>()
        val provinsiList = mutableListOf<JSONObject>()
        val kotaList = mutableListOf<JSONObject>()

        for (dinasItem in dinasList) {
            val kotaNamesString = dinasItem.tujuan
            if (kotaNamesString.contains(",")) {
                val kotaNames = kotaNamesString.split(",")
                for (kotaName in kotaNames) {
                    val cleanedKotaName = kotaName.trim().lowercase(Locale.getDefault())
                    val matchingProvinsis = provinsiList.filter {
                        it.optString("Kota").lowercase(Locale.getDefault())
                            .contains(cleanedKotaName)
                    }
                    if (matchingProvinsis.isNotEmpty()) {
                        matchingProvinsis.forEach { provinsi ->
                            val provinsiName = provinsi.optString("Kota")
                            autocompleteMap[cleanedKotaName] = provinsiName
                        }
                    } else {
                        val matchingKotas = kotaList.filter {
                            it.optString("Nama_Daerah").lowercase(Locale.getDefault())
                                .contains(cleanedKotaName)
                        }
                        matchingKotas.forEach { kotaItem ->
                            val idKota = kotaItem.optInt("ID_Kota")
                            val provinsiObject = provinsiList.find {
                                it.optInt("Id") == idKota
                            }
                            val provinsiName = provinsiObject?.optString("Kota") ?: ""
                            autocompleteMap[cleanedKotaName] = provinsiName
                        }
                    }
                }
            } else {
                val cleanedKotaName = kotaNamesString.trim().toLowerCase(Locale.getDefault())
                val matchingProvinsis = provinsiList.filter {
                    it.optString("Kota").toLowerCase(Locale.getDefault())
                        .contains(cleanedKotaName)
                }
                if (matchingProvinsis.isNotEmpty()) {
                    matchingProvinsis.forEach { provinsi ->
                        val provinsiName = provinsi.optString("Kota")
                        autocompleteMap[cleanedKotaName] = provinsiName
                    }
                } else {
                    val matchingKotas = kotaList.filter {
                        it.optString("Nama_Daerah").lowercase(Locale.getDefault())
                            .contains(cleanedKotaName)
                    }
                    matchingKotas.forEach { kotaItem ->
                        val idKota = kotaItem.optInt("ID_Kota")
                        val provinsiObject = provinsiList.find {
                            it.optInt("ID_Provinsi") == idKota
                        }
                        val provinsiName = provinsiObject?.optString("Kota") ?: ""
                        autocompleteMap[cleanedKotaName] = provinsiName
                    }
                }
            }
        }

        return autocompleteMap
    }


    private fun extractTahun(date: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(date)
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(parsedDate)
    }
}