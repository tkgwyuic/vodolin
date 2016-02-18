package com.tunesworks.vodolin.recyclerView

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class ItemTouchHelper(callback: ItemTouchHelper.Callback?) : ItemTouchHelper(callback) {
    abstract class SimpleCallback: ItemTouchHelper.SimpleCallback(0, LEFT or RIGHT) {
        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            var position = viewHolder?.adapterPosition ?: return
            when (direction) {
                RIGHT -> onRightSwipe(viewHolder, position)
                LEFT -> onLeftSwipe(viewHolder,  position)
            }
        }

        override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ACTION_STATE_SWIPE) {
                viewHolder as ToDoAdapter.ViewHolder

                viewHolder.apply {
//                    itemBackground.x = -dX
//                    if (dX > 0) { // On right swipe
//                        itemBackground.setBackgroundResource(R.color.finish)
//                        bgLeftIcon.visibility  = View.VISIBLE
//                        bgRightIcon.visibility = View.GONE
//                    } else { // On left swipe
//                        itemBackground.setBackgroundResource(R.color.edit)
//                        bgLeftIcon.visibility  = View.GONE
//                        bgRightIcon.visibility = View.VISIBLE
//                    }
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        // Swipe callback
        open fun onRightSwipe(viewHolder: RecyclerView.ViewHolder?, position: Int) {}
        open fun onLeftSwipe(viewHolder: RecyclerView.ViewHolder?, position: Int) {}
    }
}