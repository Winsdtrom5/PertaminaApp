package com.example.pertaminaapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pertaminaapp.R
import com.example.pertaminaapp.model.DinasItem
import com.example.pertaminaapp.model.LemburItem
import java.util.Locale

class DinasAdapter(private val dinasList: List<DinasItem>) :
    RecyclerView.Adapter<DinasAdapter.DinasViewHolder>() {
    private var filteredDinasList: List<DinasItem> = dinasList

    inner class DinasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define views in your item layout here
        val pekerjaanTextView: TextView = itemView.findViewById(R.id.tvTitleDinas)
        val tujuanTextView: TextView = itemView.findViewById(R.id.tvTitleTujuan)
        val tanggalView: TextView = itemView.findViewById(R.id.tvTitleTanggal)
        val cardView : CardView = itemView.findViewById(R.id.cvlistdinas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DinasViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.listdinaspekerja, parent, false)
        return DinasViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DinasViewHolder, position: Int) {
        val currentItem = filteredDinasList[position]
        // Bind data to views here
        holder.pekerjaanTextView.text = currentItem.pekerjaan
        holder.tujuanTextView.text = currentItem.tujuan
        holder.tanggalView.text = "${currentItem.tanggalberangkat} - ${currentItem.tanggalpulang}"
        if(currentItem.status == "Returned"){
            holder.cardView.setBackgroundColor(Color.parseColor("#f7adc3"))
        }else if(currentItem.status == "Diverted"){
            holder.cardView.setBackgroundColor(Color.parseColor("#ffd3a8"))
        }else if(currentItem.status == "Rejected"){
            holder.cardView.setBackgroundColor(Color.parseColor("#dd98f5"))
        }else if(currentItem.status == "Pending"){
            holder.cardView.setBackgroundColor(Color.parseColor("#fff0c4"))
        }else if(currentItem.status == "Review"){
            holder.cardView.setBackgroundColor(Color.parseColor("#d2e5f7"))
        }else{
            holder.cardView.setBackgroundColor(Color.parseColor("#a4fcc2"))
        }
    }
    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charSequenceString = charSequence.toString().toLowerCase(Locale.getDefault())
                val filteredList = if (charSequenceString.isEmpty()) {
                    dinasList
                } else {
                    dinasList.filter { item ->
                        item.pekerjaan.toLowerCase(Locale.getDefault()).contains(charSequenceString)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredDinasList = filterResults.values as List<DinasItem>
                notifyDataSetChanged()
            }
        }
    }
    fun updateFilter(filteredDinasList: List<DinasItem>) {
        this.filteredDinasList = filteredDinasList
        notifyDataSetChanged()
    }
    override fun getItemCount() = filteredDinasList.size
}
