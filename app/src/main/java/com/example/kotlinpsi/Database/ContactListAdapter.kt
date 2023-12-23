package com.example.kotlinpsi.Database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinpsi.R

class ContactListAdapter : ListAdapter<Contact, ContactListAdapter.ContactViewHolder>(CONTACTS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.date.toString()+"   "+String(current.name))
    }

    class ContactViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val contactItemView:TextView=itemView.findViewById(R.id.itemtext)

        fun bind(text:String?){
            contactItemView.text=text
        }

        companion object{
            fun create(parent: ViewGroup):ContactViewHolder{
                val view:View=LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycleritem,parent,false)
                return ContactViewHolder(view)
            }
        }
    }

    companion object{
        private val CONTACTS_COMPARATOR=object : DiffUtil.ItemCallback<Contact>(){
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return ((oldItem.date==newItem.date)&&(oldItem.name.contentEquals(newItem.name)))
            }
        }
    }
}