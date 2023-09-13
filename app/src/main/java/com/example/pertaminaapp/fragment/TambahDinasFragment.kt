package com.example.pertaminaapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.example.pertaminaapp.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class TambahDinasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tambah_dinas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read and parse kota.json and provinsi.json
        val kotaDataList = readAndParseKotaJson()
        val provinsiDataList = readAndParseProvinsiJson()

        // Combine and display the data
        val combinedDataList = combineAndFormatData(kotaDataList, provinsiDataList)

        // Create an ArrayAdapter with the combined data
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            combinedDataList
        )

        // Set the adapter to the AutoCompleteTextView
        val textviewasal = view.findViewById<AutoCompleteTextView>(R.id.acasal)
        val textviewtujuan = view.findViewById<AutoCompleteTextView>(R.id.actujuan)
        // Set the adapter to the AutoCompleteTextView
        textviewasal.setAdapter(adapter)
        textviewtujuan.setAdapter(adapter)// Set a threshold for auto-suggestions
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

    private fun combineAndFormatData(kotaList: List<JSONObject>, provinsiList: List<JSONObject>): List<String> {
        val combinedDataList = mutableListOf<String>()
        for (kotaObject in kotaList) {
            val kotaId = kotaObject.optInt("ID_Kota")
            for (provinsiObject in provinsiList) {
                val provinsiId = provinsiObject.optInt("Id")
                if (kotaId == provinsiId) {
                    val kotaName = kotaObject.optString("Nama_Daerah")
                    val provinsiName = provinsiObject.optString("Kota")
                    val combinedData = "$kotaName, $provinsiName"
                    combinedDataList.add(combinedData)
                }
            }
        }
        return combinedDataList
    }
}

