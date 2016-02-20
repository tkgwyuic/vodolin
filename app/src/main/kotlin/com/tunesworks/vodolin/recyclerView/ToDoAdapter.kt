package com.tunesworks.vodolin.recyclerView

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.shapes.Shape
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.ionicons
import com.tunesworks.vodolin.model.itemColor
import io.realm.RealmResults

class ToDoAdapter(context: Context, results: RealmResults<ToDo>, val listener: ViewHolder.ItemListener): RealmRecyclerViewAdapter<ToDo, ToDoAdapter.ViewHolder>(context, results) {
    val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        return ViewHolder(inflater.inflate(ViewHolder.LAYOUT_ID, parent, false), listener)
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


    class ViewHolder(itemView: View, val listener: ItemListener): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        companion object {
            val LAYOUT_ID = R.layout.list_item
        }

        val cardView = itemView.findViewById(R.id.card_view) as CardView
        val content = itemView.findViewById(R.id.content) as TextView
        val itemColor = itemView.findViewById(R.id.item_color)
        val itemIcon  = itemView.findViewById(R.id.item_icon) as TextView
        val itemBackground = itemView.findViewById(R.id.item_background) as RelativeLayout
        val itemForeground = itemView.findViewById(R.id.item_foreground) as RelativeLayout
        val bgLeftIcon = itemView.findViewById(R.id.bg_left_icon) as TextView
        val bgRightIcon = itemView.findViewById(R.id.bg_right_icon) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {

            }
            listener.onItemClick(this, adapterPosition)
        }

        interface ItemListener {
            fun onItemClick(holder: ViewHolder, position: Int)
        }
    }
}