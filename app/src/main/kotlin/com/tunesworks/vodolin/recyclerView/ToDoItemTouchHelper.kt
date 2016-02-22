package com.tunesworks.vodolin.recyclerView

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.tunesworks.vodolin.VoDolin

class ToDoItemTouchHelper(callback: ItemTouchHelper.Callback) : ItemTouchHelper(callback) {
    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        val pos    = parent?.getChildAdapterPosition(view) ?: -1
        val count  = parent?.adapter?.itemCount ?: 0
        val bottom = if (pos >= count-1) 72 else 0 // if last item
        outRect?.set(4, 4, 4, bottom) // left top right bottom
    }
}