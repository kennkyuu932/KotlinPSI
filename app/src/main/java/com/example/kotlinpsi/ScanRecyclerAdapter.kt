package com.example.kotlinpsi

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScanRecyclerAdapter(private val dataList: MutableList<Item>,
                          private val onItemClickListener: (Item) -> Unit
) :
    RecyclerView.Adapter<ScanRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView1: TextView = itemView.findViewById(R.id.blerecycletext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.blerecycle, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.textView1.text = item.text2

        holder.itemView.setOnClickListener {
            onItemClickListener.invoke(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    data class Item(val text1: String,val text2:String ,val device: BluetoothDevice)
}