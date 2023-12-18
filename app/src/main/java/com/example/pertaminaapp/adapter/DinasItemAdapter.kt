package com.example.pertaminaapp.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pertaminaapp.R
import com.example.pertaminaapp.fragment.DetailDinasFragment
import com.example.pertaminaapp.fragment.DetailLemburFragment
import com.example.pertaminaapp.model.DinasData
import com.example.pertaminaapp.model.Reviewer

class DinasItemAdapter (private var dinasList: List<DinasData>,private val user: Reviewer) : RecyclerView.Adapter<DinasItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listdinaspekerja, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dinasData = dinasList[position]
        // Customize the binding of data to the ViewHolder based on your LemburData structure
        holder.bind(dinasData)
    }
    fun clearData() {
        dinasList = emptyList() // or lemburList = mutableListOf()
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return dinasList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define your UI elements here (e.g., TextViews)
        val pekerjaanTextView: TextView = itemView.findViewById(R.id.tvTitleDinas)
        val tujuanTextView: TextView = itemView.findViewById(R.id.tvTitleTujuan)
        val tanggalView: TextView = itemView.findViewById(R.id.tvTitleTanggal)
        val cv : CardView = itemView.findViewById(R.id.cvlistdinas)
        fun bind(dinasData: DinasData) {
            pekerjaanTextView.text = dinasData.keterangan
            tujuanTextView.text =dinasData.tujuan
            tanggalView.text = "${dinasData.mulai} - ${dinasData.akhir}"
            cv.setOnClickListener{
                val fragment = DetailDinasFragment()
                // Pass the current lemburData to the fragment
                val bundle = Bundle()
                bundle.putParcelable("dinasData", dinasData)
                bundle.putParcelable("user", user)
                fragment.arguments = bundle

                // Replace the current fragment with DetailLemburFragment
                val transaction = (itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }
}