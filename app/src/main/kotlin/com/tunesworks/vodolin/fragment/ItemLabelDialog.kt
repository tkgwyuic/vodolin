package com.tunesworks.vodolin.fragment

import android.animation.Animator
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.value.Ionicons
import com.tunesworks.vodolin.value.ItemColor
import kotlin.properties.Delegates

class ItemLabelDialog: DialogFragment() {
    var itemColor: ItemColor by Delegates.notNull<ItemColor>()
    var ionicons:  Ionicons  by Delegates.notNull<Ionicons>()
    var colorAdapter: GridViewAdapter<ItemColor> by Delegates.notNull<GridViewAdapter<ItemColor>>()
    var iconAdapter: GridViewAdapter<Ionicons>   by Delegates.notNull<GridViewAdapter<Ionicons>>()
    var itemLabelSetListener: OnItemLabelSetListener? = null
    var alertDialog: Dialog by Delegates.notNull<Dialog>()

    override fun onAttach(context: Context?) {
        if (context is OnItemLabelSetListener) itemLabelSetListener = context
        else if (targetFragment is OnItemLabelSetListener) itemLabelSetListener = targetFragment as OnItemLabelSetListener
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val customView = LayoutInflater.from(context).inflate(R.layout.dialog_grid, null)
        val gridView   = customView.findViewById(R.id.grid_view) as GridView

        colorAdapter = GridViewAdapter(context, ItemColor.values().toList(), {
            gridView.animate().setDuration(200).setListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(p0: Animator?) {
                    gridView.adapter = iconAdapter
                    gridView.animate().setDuration(200).alpha(1f)
                }
                override fun onAnimationStart(p0: Animator?) {}
                override fun onAnimationRepeat(p0: Animator?) {}
                override fun onAnimationCancel(p0: Animator?) {}

            }).alpha(0f)

            itemColor = it
        })

        iconAdapter = GridViewAdapter(context, Ionicons.values().toList(), {
            ionicons = it
            itemLabelSetListener?.onItemLabelSet(itemColor, ionicons)
            alertDialog.dismiss()
        })

        gridView.adapter = colorAdapter

        alertDialog = AlertDialog.Builder(context)
                .setTitle("Color and Icon")
                .setView(customView)
                .setNegativeButton("Cancel", null)
                .create()

        return alertDialog
    }

    interface OnItemLabelSetListener {
        fun onItemLabelSet(itemColor: ItemColor, ionicons: Ionicons) {}
    }

    class GridViewAdapter<T>(
            context: Context,
            list: List<T>,
            val onItemSelected: (item: T) -> Unit
    ): ArrayAdapter<T>(context, LAYOUT_ID, list){
        companion object {
            val LAYOUT_ID = R.layout.grid_item
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val view = (convertView ?: LayoutInflater.from(context).inflate(LAYOUT_ID, null)) as RelativeLayout
            val textView = view.findViewById(R.id.text) as TextView
            val item = getItem(position)

            when (item) {
                is ItemColor -> {
                    textView.background = GradientDrawable().apply {
                        setColor(item.color)
                        setShape(GradientDrawable.OVAL)
                    }
                }
                is Ionicons -> {
                    textView.text = item.icon
                }
            }

            // Set listener
            view.setOnClickListener { onItemSelected.invoke(item) }

            return view
        }
    }
}