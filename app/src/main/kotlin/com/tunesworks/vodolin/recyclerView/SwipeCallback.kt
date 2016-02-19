package com.tunesworks.vodolin.recyclerView

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.tunesworks.vodolin.R

abstract class SwipeCallback: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        var position = viewHolder?.adapterPosition ?: return
        when (direction) {
            ItemTouchHelper.RIGHT -> onRightSwiped(viewHolder, position)
            ItemTouchHelper.LEFT  -> onLeftSwiped(viewHolder,  position)
        }
    }

    override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            viewHolder as ToDoAdapter.ViewHolder

            viewHolder.apply {
                itemBackground.x = -dX
                if (dX > 0) { // On right swipe
                    itemBackground.setBackgroundResource(R.color.done)
                    bgLeftIcon.visibility  = View.VISIBLE
                    bgRightIcon.visibility = View.GONE
                } else { // On left swipe
                    itemBackground.setBackgroundResource(R.color.edit)
                    bgLeftIcon.visibility  = View.GONE
                    bgRightIcon.visibility = View.VISIBLE
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    // Swipe callback
    open fun onRightSwiped(viewHolder: RecyclerView.ViewHolder?, position: Int) {}
    open fun onLeftSwiped(viewHolder: RecyclerView.ViewHolder?, position: Int) {}
}