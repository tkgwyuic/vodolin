package com.tunesworks.vodolin.recyclerView

import android.content.Context
import android.support.v7.widget.RecyclerView
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import java.util.*

abstract class RealmRecyclerViewAdapter<T: RealmObject, VH: RecyclerView.ViewHolder>(
        val context: Context,
        val realmResults: RealmResults<T>
): RecyclerView.Adapter<VH>() {
    val selectedItemPositions = ArrayList<Int>()

    class PositionAndItem<T: RealmObject>(val position: Int, var item: T)

    override fun getItemCount() = realmResults.size

    fun toggleSelection(position: Int) {
        if (selectedItemPositions.find { it == position } != null) {
            selectedItemPositions.remove(position)
        } else {
            selectedItemPositions.add(position)
            selectedItemPositions.sortedByDescending { it.compareTo(position) }
        }
        notifyItemChanged(position)
    }

    fun getSelectedItems() : RealmList<T> {
        val items = RealmList<T>()
        selectedItemPositions.forEach { items.add(realmResults[it]) }
        return items
    }

    fun clearSelections() {
        selectedItemPositions.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItemCount() = selectedItemPositions.size

    fun notifySelectedItemInserted() {
        selectedItemPositions.reversed().forEach {
            notifyItemInserted(it)
        }
    }
}