package com.example.pertaminaapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pertaminaapp.R
import com.example.pertaminaapp.adapter.LemburAdapter
import com.example.pertaminaapp.connection.eworks
import com.example.pertaminaapp.model.LemburItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.Locale

class DaftarDinasFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LemburAdapter
    private val lemburList: MutableList<LemburItem> = mutableListOf()
    private var kode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_daftar_dinas, container, false)
        kode = arguments?.getString("kode")
        recyclerView = view.findViewById(R.id.rv1)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with an empty list for now
        adapter = LemburAdapter(lemburList)
        recyclerView.adapter = adapter

        // Fetch data from MySQL
        fetchDataFromMySQL()
// Inside DaftarLemburFragment onCreateView

        val filterIcon = view.findViewById<ImageView>(R.id.filter_icon)
        filterIcon.setOnClickListener {
            // Show the filter dialog when the icon is clicked
            showFilterDialog()
        }

        return view
    }
    private fun showFilterDialog() {
        val fragmentManager = childFragmentManager
        val (uniqueBulanList, uniqueTahunList) = getBulanAndTahunLists()
        val filterDialogFragment = FilterDialogFragment.newInstance(uniqueBulanList, uniqueTahunList)
        filterDialogFragment.show(fragmentManager, "com.example.pertaminaapp.fragment.FilterDialogFragment")
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
}