package com.tunesworks.vodolin.recyclerView

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tunesworks.vodolin.model.ToDo
import io.realm.RealmResults

class ToDoAdapter(ctxt: Context, results: RealmResults<ToDo>): RealmRecyclerViewAdapter<ToDo, ToDoAdapter.ViewHolder>(ctxt, results) {
    val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        return ViewHolder(inflater.inflate(ViewHolder.LAYOUT_ID, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.textView?.text = realmResults[position].content
    }

    override fun getItemCount() = realmResults.size


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        companion object {
            val LAYOUT_ID = android.R.layout.simple_list_item_1
        }

        val textView = itemView.findViewById(android.R.id.text1) as TextView
    }
}