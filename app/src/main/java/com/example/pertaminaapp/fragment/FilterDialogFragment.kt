package com.example.pertaminaapp.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.pertaminaapp.R

class FilterDialogFragment : DialogFragment() {
    companion object {
        const val KEY_BULAN_LIST = "bulan_list"
        const val KEY_TAHUN_LIST = "tahun_list"

        fun newInstance(bulanList: List<String>, tahunList: List<String>): FilterDialogFragment {
            val fragment = FilterDialogFragment()
            val args = Bundle()
            args.putStringArrayList(KEY_BULAN_LIST, ArrayList(bulanList))
            args.putStringArrayList(KEY_TAHUN_LIST, ArrayList(tahunList))
            fragment.arguments = args
            return fragment
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

        // Set up your filter options, e.g., spinners, EditTexts, etc., in the view
        val spinnerBulan = view.findViewById<Spinner>(R.id.dropdownbulan)
        val spinnerTahun = view.findViewById<Spinner>(R.id.dropdowntahun)

        // Create adapters for the spinners
        val bulanAdapter = uniqueBulanList?.let { ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it.toList()) }
        val tahunAdapter = uniqueTahunList?.let { ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it.toList()) }

        // Set dropdown view resource for adapters
        bulanAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tahunAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set adapters for the spinners
        spinnerBulan.adapter = bulanAdapter
        spinnerTahun.adapter = tahunAdapter

        // Find the Button by its ID
        val buttonApply = view.findViewById<Button>(R.id.buttonApply)

        // Add a click listener to the Apply button
        buttonApply.setOnClickListener {
            // Implement logic to apply filters and update your RecyclerView
            // Dismiss the dialog when filters are applied
            dismiss()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(true)
        }
    }
}
