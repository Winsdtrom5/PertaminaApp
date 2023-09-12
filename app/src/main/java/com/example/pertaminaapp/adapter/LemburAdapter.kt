package com.example.pertaminaapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pertaminaapp.R
import com.example.pertaminaapp.model.LemburItem

class LemburAdapter(private val lemburList: List<LemburItem>) :
    RecyclerView.Adapter<LemburAdapter.LemburViewHolder>() {

    inner class LemburViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define views in your item layout here
        val pekerjaanTextView: TextView = itemView.findViewById(R.id.tvTitle1)
        val tanggalTextView: TextView = itemView.findViewById(R.id.tvTitle2)
        val jamTextView: TextView = itemView.findViewById(R.id.tvTitle3)
        val cardView : CardView = itemView.findViewById(R.id.cvlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LemburViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.listlemburpekerja, parent, false)
        return LemburViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LemburViewHolder, position: Int) {
        val currentItem = lemburList[position]
        // Bind data to views here
        holder.pekerjaanTextView.text = currentItem.pekerjaan
        holder.tanggalTextView.text = currentItem.tanggal
        holder.jamTextView.text = "${currentItem.jammasuk} - ${currentItem.jamkeluar}"
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

    override fun getItemCount() = lemburList.size
}
