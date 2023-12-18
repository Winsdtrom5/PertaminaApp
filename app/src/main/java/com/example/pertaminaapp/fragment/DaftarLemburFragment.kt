package com.example.pertaminaapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pertaminaapp.R
import com.example.pertaminaapp.adapter.LemburAdapter
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.model.LemburItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Locale

class DaftarLemburFragment : Fragment(), FilterDialogFragment.FilterDialogListener  {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LemburAdapter
    private val lemburList: MutableList<LemburItem> = mutableListOf()
    private var kode: String? = null
    private lateinit var searchText: EditText
    private var selectedStatus: String? = null
    private var selectedBulan: String? = null
    private var selectedTahun: String? = null
    private var isFilterApplied = false
    private var lastSelectedStatus: String? = null
    private var lastSelectedBulan: String? = null
    private var lastSelectedTahun: String? = null
    private lateinit var textDataNotFound: TextView
    private var filteredLemburList: List<LemburItem> = mutableListOf()
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_daftar_lembur, container, false)
        kode = arguments?.getString("kode")
        recyclerView = view.findViewById(R.id.rv1)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
// Inside DaftarLemburFragment onCreateView
        searchText= view.findViewById<EditText>(R.id.search_edit_text)
        textDataNotFound = view.findViewById<TextView>(R.id.not_found)
        val filterIcon = view.findViewById<ImageView>(R.id.filter_icon)
        filterIcon.setOnClickListener {
            // Show the filter dialog when the icon is clicked
            showFilterDialog()
        }
        val imageView = view.findViewById<ImageView>(R.id.search_icon)
        imageView.setOnClickListener {
            val searchText = searchText.text.toString().toLowerCase(Locale.getDefault())

            // Launch a coroutine to perform the database search
            CoroutineScope(Dispatchers.Main).launch {
                // Perform a database search and get the filtered data
                val filteredData = performDatabaseSearch(searchText) // Replace with your database search function

                // Update the adapter with the filtered data
                adapter.updateFilter(filteredData)

                // Show or hide textDataNotFound based on whether data is found
                textDataNotFound.visibility = if (filteredData.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        adapter = LemburAdapter(lemburList)
        recyclerView.adapter = adapter

        // Fetch data from MySQL
        fetchDataFromMySQL()
        return view
    }
    private suspend fun performDatabaseSearch(searchText: String): List<LemburItem> {
        return withContext(Dispatchers.IO) {
            val filteredData = mutableListOf<LemburItem>()

            // Execute a database query based on the searchText
            // Replace this with your database query logic using JDBC
            val connection = eworks.getConnection()
            val query = "SELECT pekerjaan,tanggal,jammasuk, jamkeluar FROM spkl WHERE nomor LIKE ?"
            val preparedQuery = connection?.prepareStatement(query)
            if (preparedQuery != null) {
                preparedQuery.setString(1, "%$searchText%")
            }
            val resultSet = preparedQuery?.executeQuery()
            if (resultSet != null) {
                while (resultSet.next()) {
                    // Populate filteredData with the results
                    val lemburItem = LemburItem(
                        resultSet.getString("pekerjaan"),
                        resultSet.getString("tanggal"),
                        resultSet.getString("jammasuk"),
                        resultSet.getString("jamkeluar"),
                        resultSet.getString("jamkeluar")
                    )
                    filteredData.add(lemburItem)
                }
            }

            filteredData
        }
    }

    private fun showFilterDialog() {
        val fragmentManager = childFragmentManager
        val (uniqueBulanList, uniqueTahunList) = getBulanAndTahunLists()
        val filterDialogFragment = FilterDialogFragment.newInstance(
            uniqueBulanList,
            uniqueTahunList,
            lastSelectedStatus, // Pass the last selected values as arguments
            lastSelectedBulan,
            lastSelectedTahun
        )
        filterDialogFragment.show(
            fragmentManager,
            "com.example.pertaminaapp.fragment.FilterDialogFragment"
        )
    }

    private fun applyFilters() {
        // Filter your data based on the selected filters
        filteredLemburList = lemburList.filter { item ->
            (selectedStatus == null || item.status == selectedStatus) &&
                    (selectedBulan == null || extractMonth(selectedBulan!!) == extractBulan(item.tanggal)) &&
                    (selectedTahun == null || selectedTahun == extractTahun(item.tanggal))
        }
        if (filteredLemburList.isEmpty()) {
            adapter.updateFilter(emptyList())
            textDataNotFound.visibility = View.VISIBLE
        } else {
            textDataNotFound.visibility = View.GONE
            // Update the adapter with the filtered list
            adapter.updateFilter(filteredLemburList)
        }
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
    private fun extractBulan(date: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(date)
        return SimpleDateFormat("MM", Locale.getDefault()).format(parsedDate)
    }

    private fun extractTahun(date: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = dateFormat.parse(date)
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(parsedDate)
    }

    override fun onFilterApplied(selectedStatus: String, selectedBulan: String, selectedTahun: String) {
        this.selectedStatus = if (selectedStatus.isBlank()) null else selectedStatus
        this.selectedBulan = if (selectedBulan.isBlank()) null else selectedBulan
        this.selectedTahun = if (selectedTahun.isBlank()) null else selectedTahun

        // Apply the filters to the originalLemburList
        applyFilters()
        isFilterApplied = true // Filters are applied

        // Save the last selected filter values
        lastSelectedStatus = selectedStatus
        lastSelectedBulan = selectedBulan
        lastSelectedTahun = selectedTahun
    }

    private fun getBulanAndTahunLists(): Pair<List<String>, List<String>> {
        // Extract bulan and tahun values from lemburList and create lists
        val bulanList = ArrayList<String>()
        val tahunList = ArrayList<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (lemburItem in lemburList) {
            try {
                val tanggal = dateFormat.parse(lemburItem.tanggal)
                val bulanNumber = SimpleDateFormat("MM", Locale.getDefault()).format(tanggal)
                val tahun = SimpleDateFormat("yyyy", Locale.getDefault()).format(tanggal)

                if (!bulanList.contains(bulanNumber)) {
                    bulanList.add(bulanNumber)
                }
                if (!tahunList.contains(tahun)) {
                    tahunList.add(tahun)
                }
            } catch (e: Exception) {
                Log.e("Error", "Error parsing date: ${lemburItem.tanggal}")
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


    private fun fetchDataFromMySQL() {
        setLoading(true)
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            try {
                val connection = eworks.getConnection()
                if (connection != null) {
                    val query = "SELECT * FROM spkl WHERE kode_pekerja = ?"
                    val preparedStatement: PreparedStatement = connection.prepareStatement(query)
                    preparedStatement.setString(1, kode)
                    val resultSet: ResultSet = preparedStatement.executeQuery()

                    // Create a temporary list to store the data
                    val tempList: MutableList<LemburItem> = mutableListOf()

                    while (resultSet.next()) {
                        val pekerjaan = resultSet.getString("pekerjaan")
                        Log.d("A", pekerjaan)
                        val jammasuk = resultSet.getString("mulai")
                        val jamkeluar = resultSet.getString("akhir")
                        val tanggal = resultSet.getString("tanggal")
                        val status = resultSet.getString("status")
                        val lemburItem = LemburItem(pekerjaan, tanggal, jammasuk, jamkeluar, status)

                        // Add each item to the temporary list
                        tempList.add(lemburItem)
                    }

                    // Update the UI on the main thread with the complete list
                    withContext(Dispatchers.Main) {
                        // Clear the existing list and add all items from tempList
                        lemburList.clear()
                        lemburList.addAll(tempList)
                        adapter.notifyDataSetChanged()
                        if (lemburList.isEmpty()) {
                            textDataNotFound.visibility = View.VISIBLE
                        } else {
                            textDataNotFound.visibility = View.GONE
                        }
                        isFilterApplied = false
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
            setLoading(false)
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
}

