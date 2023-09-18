package com.example.pertaminaapp.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.pertaminaapp.R

class FilterDinasFragment : DialogFragment() {
    companion object {
        const val KEY_KOTA_LIST = "kota_list"
        const val KEY_BULAN_LIST = "bulan_list"
        const val KEY_TAHUN_LIST = "tahun_list"
        const val KEY_LAST_SELECTED_BULAN = "last_bulan_list"
        const val KEY_LAST_SELECTED_KOTA = "last_kota_list"
        const val KEY_LAST_SELECTED_STATUS = "last_status_list"
        const val KEY_LAST_SELECTED_TAHUN = "last_tahun_list"
        fun newInstance(
            kotaList: List<String>,
            bulanList: List<String>,
            tahunList: List<String>,
            lastSelectedKota: String?,
            lastSelectedTahun: String?,
            lastSelectedStatus: String?,
            lastSelectedBulan: String?
        ): FilterDinasFragment{
            val fragment = FilterDinasFragment()
            val args = Bundle()
            args.putStringArrayList(KEY_KOTA_LIST, ArrayList(kotaList))
            args.putStringArrayList(KEY_BULAN_LIST, ArrayList(bulanList))
            args.putStringArrayList(KEY_TAHUN_LIST, ArrayList(tahunList))
            args.putString(KEY_LAST_SELECTED_STATUS, lastSelectedStatus) // Store last selected values in arguments
            args.putString(KEY_LAST_SELECTED_KOTA, lastSelectedKota)
            args.putString(KEY_LAST_SELECTED_TAHUN, lastSelectedTahun)
            args.putString(FilterDialogFragment.KEY_LAST_SELECTED_BULAN, lastSelectedBulan)
            fragment.arguments = args
            return fragment
        }
    }
    interface FilterDialogListener {
        fun onFilterApplied(selectedStatus: String, selectedKota: String, selectedTahun: String,selectedBulan: String)
    }
    private var filterListener: FilterDialogListener? = null
    private var lastSelectedStatus: String? = null
    private var lastSelectedKota: String? = null
    private var lastSelectedTahun: String? = null
    private var lastSelectedBulan: String? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Check if the parent Fragment implements the FilterDialogListener interface
        if (parentFragment is FilterDialogListener) {
            filterListener = parentFragment as FilterDialogListener
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter_dinas, container, false)

        // Retrieve  and tahun lists from arguments
        lastSelectedStatus = arguments?.getString(KEY_LAST_SELECTED_STATUS)
        lastSelectedKota = arguments?.getString(KEY_LAST_SELECTED_KOTA)
        lastSelectedTahun = arguments?.getString(KEY_LAST_SELECTED_TAHUN)
        lastSelectedBulan = arguments?.getString(KEY_LAST_SELECTED_BULAN)
// Set adapters for the AutoCompleteTextViews

        val autoCompleteKota = view.findViewById<AutoCompleteTextView>(R.id.ackota)
        val autoCompleteBulan = view.findViewById<AutoCompleteTextView>(R.id.acbulan)
        val autoCompleteTahun = view.findViewById<AutoCompleteTextView>(R.id.actahun)
        val autoCompleteStatus = view.findViewById<AutoCompleteTextView>(R.id.acstatus)
        val uniqueBulanList = arguments?.getStringArrayList(KEY_BULAN_LIST)
        val statusOptions = arrayOf("Approve", "Returned", "Diverted", "Rejected", "Pending", "Review")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions)
        val kotaList = arguments?.getStringArrayList(KEY_KOTA_LIST)
        val tahunList = arguments?.getStringArrayList(KEY_TAHUN_LIST)
        val kotaAdapter = kotaList?.let {
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it.toList())
        }
        val tahunAdapter = tahunList?.let {
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it.toList())
        }
        val bulanAdapter = uniqueBulanList?.let {
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it.toList())
        }
        autoCompleteStatus.setText(lastSelectedStatus)
        autoCompleteKota.setText(lastSelectedKota)
        autoCompleteTahun.setText(lastSelectedTahun)
        autoCompleteBulan.setText(lastSelectedBulan)
        autoCompleteKota.setAdapter(kotaAdapter) // Set the adapter for kota AutoCompleteTextView
        autoCompleteTahun.setAdapter(tahunAdapter)
        autoCompleteStatus.setAdapter(statusAdapter)
        autoCompleteBulan.setAdapter(bulanAdapter)
        // Find the Button by its ID
        val buttonApply = view.findViewById<Button>(R.id.buttonApply)
        val buttonReset = view.findViewById<Button>(R.id.buttonReset)
        // Add a click listener to the Apply button
        buttonApply.setOnClickListener {
            val selectedStatus = autoCompleteStatus.text.toString()
            val selectedKota = autoCompleteKota.text.toString()
            val selectedTahun = autoCompleteTahun.text.toString()
            val selectedBulan = autoCompleteBulan.text.toString()
            // Check if the listener is not null and then invoke the callback
            filterListener?.onFilterApplied(selectedStatus, selectedKota, selectedTahun,selectedBulan)

            // Dismiss the dialog when filters are applied
            dismiss()
        }
        // Add a click listener to the Reset button to clear filters
        buttonReset.setOnClickListener {
            autoCompleteStatus.text.clear()
            autoCompleteKota.text.clear()
            autoCompleteTahun.text.clear()
            autoCompleteBulan.text.clear()
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(true)
        }
    }
}
