package com.example.pertaminaapp.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.example.pertaminaapp.R
import com.example.pertaminaapp.model.UnitItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

class TambahLemburFragment : Fragment() {
    private lateinit var posisi : AutoCompleteTextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tambah_lembur, container, false)
        val tilTanggal = view.findViewById<TextInputLayout>(R.id.TITanggal)
        val edtanggal = tilTanggal.editText
        val tilMasuk = view.findViewById<TextInputLayout>(R.id.TIMasuk)
        val edmasuk = tilMasuk.editText
        val tilKeluar = view.findViewById<TextInputLayout>(R.id.TIKeluar)
        val edkeluar = tilKeluar.editText
        posisi = view.findViewById(R.id.autoCompletePosisi)
        val unitList = listOf(
            UnitItem("10017275", "General Manager"),
            UnitItem("10017276", "Maintenance"),
            UnitItem("10017279", "Procurement"),
            UnitItem("10017281", "Operating & Manufacturing"),
            UnitItem("10017285", "Engineering & Development"),
            UnitItem("10017286", "HSSE"),
            UnitItem("10019430", "QM"),
            UnitItem("10039450", "Human Capital"),
            UnitItem("10057049", "Asset Management"),
            UnitItem("10059224", "ICT"),
            UnitItem("10064918", "Finance")
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, unitList)
        posisi.setAdapter(adapter)
        edtanggal?.let { editText ->
            // Set up click listener for the end icon of Tanggal
            tilTanggal.setEndIconOnClickListener {
                // Show date picker dialog
                showDatePickerDialog(editText)
            }
        }
        edmasuk?.let { editText ->
            // Set up click listener for the end icon of Tanggal
            tilMasuk.setEndIconOnClickListener {
                // Show date picker dialog
                showTimePickerDialog(editText)
            }
        }
        edkeluar?.let { editText ->
            // Set up click listener for the end icon of Tanggal
            tilKeluar.setEndIconOnClickListener {
                // Show date picker dialog
                showTimePickerDialog(editText)
            }
        }
        return view
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                val formattedTime = "$selectedHour:$selectedMinute"
                editText.setText(formattedTime)
            },
            hour,
            minute,
            true // 24-hour format
        )

        timePickerDialog.show()
    }
}
