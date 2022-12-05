package com.ashomapp.presentation.stockDetails.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.R

class ItemAdapter(val numbers: IntArray): RecyclerView.Adapter<ItemVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH
            = ItemVH(LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false))

    override fun getItemCount(): Int = numbers.size

    override fun onBindViewHolder(holder: ItemVH, position: Int) {
        holder.setNumber(numbers[position])
    }
}

class ItemVH(itemView: View): RecyclerView.ViewHolder(itemView) {
    fun setNumber(number: Int) {
        (itemView as TextView).text = number.toString()
    }
}