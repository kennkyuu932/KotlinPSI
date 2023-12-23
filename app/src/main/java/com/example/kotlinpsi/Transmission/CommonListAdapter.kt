package com.example.kotlinpsi.Transmission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinpsi.Database.Contact
import com.example.kotlinpsi.R

class CommonListAdapter : ListAdapter<Contact, CommonListAdapter.CommonViewHolder>(COMMON_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        return CommonViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.date.toString())
    }


    class CommonViewHolder(itemview : View):RecyclerView.ViewHolder(itemview){
        private val commonItemView:TextView=itemview.findViewById(R.id.commontext)

        fun bind(text:String?){
            commonItemView.text=text
        }

        companion object{
            fun create(parent:ViewGroup):CommonViewHolder{
                val view:View=LayoutInflater.from(parent.context)
                    .inflate(R.layout.commonrecycler,parent,false)
                return CommonViewHolder(view)
            }
        }
    }
    companion object{
        private val COMMON_COMPARATOR = object : DiffUtil.ItemCallback<Contact>(){
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.date==newItem.date
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.date==newItem.date
            }
        }
    }
}