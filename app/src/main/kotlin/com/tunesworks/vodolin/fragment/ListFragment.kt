package com.tunesworks.vodolin.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.recyclerView.ItemTouchHelper
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
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback() {
            override fun onLeftSwipe(viewHolder: RecyclerView.ViewHolder?, position: Int) {
                super.onLeftSwipe(viewHolder, position)
            }

            override fun onRightSwipe(viewHolder: RecyclerView.ViewHolder?, position: Int) {
                super.onRightSwipe(viewHolder, position)
            }
        }).apply { attachToRecyclerView(recycler_view) }

        // Set LayoutManager and Adapter
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity).apply { orientation = LinearLayoutManager.VERTICAL }
            adapter = todoAdapter
            addItemDecoration(itemTouchHelper)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        VoDolin.observers.deleteObserver(observer)
        realm.close()
    }
}