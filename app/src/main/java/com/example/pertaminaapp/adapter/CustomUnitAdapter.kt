package com.example.pertaminaapp.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.pertaminaapp.model.UnitItem

class CustomUnitAdapter(context: Context, resource: Int, items: List<UnitItem>) :
    ArrayAdapter<UnitItem>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val unitItem = getItem(position)

        // Set the text displayed in the dropdown to the unit name
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = unitItem?.name

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val unitItem = getItem(position)

        // Set the text displayed in the dropdown list to the unit name
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = unitItem?.name

        return view
    }
}