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

class FilterDialogFragment : DialogFragment() {
    companion object {
        const val KEY_BULAN_LIST = "bulan_list"
        const val KEY_TAHUN_LIST = "tahun_list"
        const val KEY_LAST_SELECTED_BULAN = "last_bulan_list"
        const val KEY_LAST_SELECTED_STATUS = "last_status_list"
        const val KEY_LAST_SELECTED_TAHUN = "last_tahun_list"
        fun newInstance(
            bulanList: List<String>,
            tahunList: List<String>,
            lastSelectedStatus: String?, // Receive last selected values as arguments
            lastSelectedBulan: String?,
            lastSelectedTahun: String?
        ): FilterDialogFragment {
            val fragment = FilterDialogFragment()
            val args = Bundle()
            args.putStringArrayList(KEY_BULAN_LIST, ArrayList(bulanList))
            args.putStringArrayList(KEY_TAHUN_LIST, ArrayList(tahunList))
            args.putString(KEY_LAST_SELECTED_STATUS, lastSelectedStatus) // Store last selected values in arguments
            args.putString(KEY_LAST_SELECTED_BULAN, lastSelectedBulan)
            args.putString(KEY_LAST_SELECTED_TAHUN, lastSelectedTahun)
            fragment.arguments = args
            return fragment
        }
    }
    interface FilterDialogListener {
        fun onFilterApplied(selectedStatus: String, selectedBulan: String, selectedTahun: String)
    }
    private var filterListener: FilterDialogListener? = null
    private var lastSelectedStatus: String? = null
    private var lastSelectedBulan: String? = null
    private var lastSelectedTahun: String? = null
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
        val view = inflater.inflate(R.layout.dialog_filter, container, false)

        // Retrieve bulan and tahun lists from arguments
        val uniqueBulanList = arguments?.getStringArrayList(KEY_BULAN_LIST)
        val uniqueTahunList = arguments?.getStringArrayList(KEY_TAHUN_LIST)

        lastSelectedStatus = arguments?.getString(KEY_LAST_SELECTED_STATUS)
        lastSelectedBulan = arguments?.getString(KEY_LAST_SELECTED_BULAN)
        lastSelectedTahun = arguments?.getString(KEY_LAST_SELECTED_TAHUN)
        // Set up your filter options, e.g., AutoCompleteTextViews, EditTexts, etc., in the view
        val autoCompleteBulan = view.findViewById<AutoCompleteTextView>(R.id.acbulan)
        val autoCompleteTahun = view.findViewById<AutoCompleteTextView>(R.id.actahun)
        val autoCompleteStatus = view.findViewById<AutoCompleteTextView>(R.id.acstatus)

        val statusOptions = arrayOf("Approve","Returned", "Diverted", "Rejected", "Pending", "Review")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions)
    // Create adapters for the AutoCompleteTextViews
        val bulanAdapter = uniqueBulanList?.let { ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it.toList()) }
        val tahunAdapter = uniqueTahunList?.let { ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, it.toList()) }
        autoCompleteStatus.setText(lastSelectedStatus)
        autoCompleteBulan.setText(lastSelectedBulan)
        autoCompleteTahun.setText(lastSelectedTahun)
    // Set adapters for the AutoCompleteTextViews
        autoCompleteBulan.setAdapter(bulanAdapter)
        autoCompleteTahun.setAdapter(tahunAdapter)
        autoCompleteStatus.setAdapter(statusAdapter)
    // Find the Button by its ID
        val buttonApply = view.findViewById<Button>(R.id.buttonApply)
        val buttonReset = view.findViewById<Button>(R.id.buttonReset)
    // Add a click listener to the Apply button
        buttonApply.setOnClickListener {
            val selectedStatus = autoCompleteStatus.text.toString()
            val selectedBulan = autoCompleteBulan.text.toString()
            val selectedTahun = autoCompleteTahun.text.toString()

            // Check if the listener is not null and then invoke the callback
            filterListener?.onFilterApplied(selectedStatus, selectedBulan, selectedTahun)

            // Dismiss the dialog when filters are applied
            dismiss()
        }
        // Add a click listener to the Reset button to clear filters
        buttonReset.setOnClickListener {
            autoCompleteStatus.text.clear()
            autoCompleteBulan.text.clear()
            autoCompleteTahun.text.clear()
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(true)
        }
    }
}
