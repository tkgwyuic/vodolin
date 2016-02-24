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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.ionicons
import com.tunesworks.vodolin.model.itemColor
import io.realm.RealmResults

class ToDoAdapter(context: Context, results: RealmResults<ToDo>, val listener: ViewHolder.ItemListener): RealmRecyclerViewAdapter<ToDo, ToDoAdapter.ViewHolder>(context, results) {
    val inflater = LayoutInflater.from(context)
    val VIEW_TYPE_DEFAULT = 0
    val VIEW_TYPE_SELECTED = 1

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        return ViewHolder(inflater.inflate(ViewHolder.LAYOUT_ID, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val todo = realmResults[position]
        val viewType = getItemViewType(position)

        holder?.apply {
            // Set content
            content.text = todo.content

            // Set memo
            memo.apply {
                if (todo.memo == "") visibility = View.GONE
                else {
                    visibility = View.VISIBLE
                    text = todo.memo
                }
            }

            // Set item label background
            itemLabel.background = GradientDrawable().apply {
                setColor(todo.itemColor.color)
                setShape(GradientDrawable.OVAL)
            }

            // Set foreground color and item label icon
            when (viewType) {
                VIEW_TYPE_DEFAULT  -> {
                    itemForeground.setBackgroundResource(R.color.list_item)
                    itemLabel.text = todo.ionicons.icon
                }
                VIEW_TYPE_SELECTED -> {
                    itemForeground.setBackgroundResource(R.color.list_item_selected)
                    itemLabel.setText(R.string.icon_done)
                }
            }

            // Set item action
            if (todo.deadline != null) {
                itemAction.apply{
                    visibility = View.VISIBLE
                    if (todo.isNotify) setText(R.string.icon_notification_on)
                    else setText(R.string.icon_notification_off)
                }

            } else {
                itemAction.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = realmResults.size

    override fun getItemViewType(position: Int): Int {
        if (isSelected(position)) return VIEW_TYPE_SELECTED
        else return VIEW_TYPE_DEFAULT
    }


    class ViewHolder(itemView: View, val listener: ItemListener): RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        companion object {
            val LAYOUT_ID = R.layout.list_item
        }

        val content = itemView.findViewById(R.id.content) as TextView
        val memo = itemView.findViewById(R.id.memo) as TextView
        val itemLabel = itemView.findViewById(R.id.item_label) as TextView
        val itemBackground = itemView.findViewById(R.id.item_background) as RelativeLayout
        val itemForeground = itemView.findViewById(R.id.item_foreground) as RelativeLayout
        val bgLeftIcon = itemView.findViewById(R.id.bg_left_icon) as TextView
        val bgRightIcon = itemView.findViewById(R.id.bg_right_icon) as TextView
        val itemAction = itemView.findViewById(R.id.item_action) as TextView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            itemLabel.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                itemLabel.id -> listener.onItemSelect(this, adapterPosition)
                else -> listener.onItemClick(this, adapterPosition)
            }
        }

        override fun onLongClick(view: View?): Boolean {
            listener.onItemSelect(this, adapterPosition)
            return true
        }

        interface ItemListener {
            fun onItemClick(holder: ViewHolder,  position: Int)
            fun onItemSelect(holder: ViewHolder, position: Int)
        }
    }
}