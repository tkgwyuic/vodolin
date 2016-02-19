package com.tunesworks.vodolin.fragment

import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.recyclerView.SwipeCallback
import com.tunesworks.vodolin.recyclerView.ToDoAdapter
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.*
import kotlin.properties.Delegates

class ListFragment: Fragment() {
    companion object {
        val KEY_ITEM_COLOR_NAME = "KEY_ITEM_COLOR_NAME"

        fun newInstance(itemColor: ItemColor): Fragment {
            val args     = Bundle().apply {
                putString(KEY_ITEM_COLOR_NAME, itemColor.toString())
            }

            return ListFragment().apply {
                arguments = args
            }
        }
    }

    // Observer for update realmResults
    val observer = Observer { observable, data ->
        Log.d("Observer", "update")
        if (data is ChangeToDoEvent && data.itemColorName == arguments.getString(KEY_ITEM_COLOR_NAME)) {
            Log.d(data.itemColorName, "notifyDataSetChanged")
            todoAdapter.notifyDataSetChanged()
        }
    }
    // Event class
    data class ChangeToDoEvent(val itemColorName: String)

    var realm:        Realm by Delegates.notNull<Realm>()
    var realmResults: RealmResults<ToDo> by Delegates.notNull<RealmResults<ToDo>>()
    val todoAdapter: ToDoAdapter by lazy { ToDoAdapter(activity, realmResults) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init
        realm        = Realm.getInstance(activity)
        realmResults = realm.where(ToDo::class.java).equalTo(ToDo::itemColorName.name, arguments.getString(KEY_ITEM_COLOR_NAME)).findAll()

        // Add observer
        VoDolin.observers.addObserver(observer)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ith = object : ItemTouchHelper(object : SwipeCallback(){

        }){
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                val pos    = parent?.getChildAdapterPosition(view)
                val top    = if (pos == 0) 16 else 0 // If first item
                val bottom = if (pos == todoAdapter.itemCount - 1) 72 else 16 // if last item
                outRect?.set(8, top, 8, bottom) // left top right bottom
            }
        }.apply { attachToRecyclerView(recycler_view) }

        // Set LayoutManager and Adapter
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity).apply { orientation = LinearLayoutManager.VERTICAL }
            adapter = todoAdapter
            addItemDecoration(ith)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        VoDolin.observers.deleteObserver(observer)
        realm.close()
    }
}