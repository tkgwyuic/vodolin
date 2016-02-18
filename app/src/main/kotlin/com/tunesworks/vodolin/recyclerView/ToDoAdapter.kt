package com.tunesworks.vodolin.recyclerView

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.shapes.Shape
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.ionicons
import com.tunesworks.vodolin.model.itemColor
import com.tunesworks.vodolin.model.octicons
import io.realm.RealmResults

class ToDoAdapter(ctxt: Context, results: RealmResults<ToDo>): RealmRecyclerViewAdapter<ToDo, ToDoAdapter.ViewHolder>(ctxt, results) {
    val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        return ViewHolder(inflater.inflate(ViewHolder.LAYOUT_ID, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val todo = realmResults[position]
        holder?.apply {
            content.text = realmResults[position].content
            itemColor.background = GradientDrawable().apply {
                setColor(todo.itemColor.color)
                setShape(GradientDrawable.OVAL)
            }
            itemIcon.text = todo.ionicons.icon
        }
    }

    override fun getItemCount() = realmResults.size


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        companion object {
            val LAYOUT_ID = R.layout.list_item
        }

        val content = itemView.findViewById(R.id.content) as TextView
        val itemColor = itemView.findViewById(R.id.item_color)
        val itemIcon  = itemView.findViewById(R.id.item_icon) as TextView
    }
}