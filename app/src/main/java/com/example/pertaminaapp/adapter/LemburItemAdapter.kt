package com.example.pertaminaapp.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pertaminaapp.R
import com.example.pertaminaapp.fragment.DetailLemburFragment
import com.example.pertaminaapp.model.LemburData
import com.example.pertaminaapp.model.Reviewer
import com.example.pertaminaapp.model.User


class LemburItemAdapter(private var lemburList: List<LemburData>,private val user: Reviewer) : RecyclerView.Adapter<LemburItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listlemburpekerja, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lemburData = lemburList[position]
        // Customize the binding of data to the ViewHolder based on your LemburData structure
        holder.bind(lemburData)
    }

    fun clearData() {
        lemburList = emptyList() // or lemburList = mutableListOf()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return lemburList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define your UI elements here (e.g., TextViews)
        private val pekerjaanTextView: TextView = itemView.findViewById(R.id.tvTitle1)
        private val tanggalTextView: TextView = itemView.findViewById(R.id.tvTitle2)
        private val jamTextView: TextView = itemView.findViewById(R.id.tvTitle3)
        private val cv : CardView = itemView.findViewById(R.id.cvlist)
        fun bind(lemburData: LemburData) {
            // Bind the data to your UI elements
            pekerjaanTextView.text = lemburData.pekerjaan
            tanggalTextView.text = lemburData.tanggal
            jamTextView.text = "${lemburData.mulai} - ${lemburData.akhir}"
            cv.setOnClickListener {
                val fragment = DetailLemburFragment()
                // Pass the current lemburData to the fragment
                val bundle = Bundle()
                bundle.putParcelable("lemburData", lemburData)
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
