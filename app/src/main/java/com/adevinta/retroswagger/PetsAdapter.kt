package com.adevinta.retroswagger

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.pet_list_item.view.pet_name


class PetsAdapter(val items : List<Pet>, val context: Context) : RecyclerView.Adapter<PetsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.pet_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PetsAdapter.ViewHolder, position: Int) {
        holder.petName.text = items[position].name
    }


    open class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val petName: TextView = view.pet_name
    }
}