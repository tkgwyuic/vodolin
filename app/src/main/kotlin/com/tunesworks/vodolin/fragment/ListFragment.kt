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
import com.tunesworks.vodolin.ItemColor
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.recyclerView.ToDoAdapter
import io.realm.Realm
import io.realm.RealmResults
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


    val observer = Observer { observable, data ->
        if (data is ChangeToDoEvent &&
                data.itemColorName.compareTo(arguments.getString(KEY_ITEM_COLOR_NAME)) == 0) {
            Log.d(data.itemColorName, "notifyDataSetChanged")
            adapter.notifyDataSetChanged()
        }
    }
    data class ChangeToDoEvent(val itemColorName: String)

    var realm:        Realm by Delegates.notNull<Realm>()
    var realmResults: RealmResults<ToDo> by Delegates.notNull<RealmResults<ToDo>>()
    var adapter:      ToDoAdapter by Delegates.notNull<ToDoAdapter>()
    var recyclerView: RecyclerView by Delegates.notNull<RecyclerView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm        = Realm.getInstance(activity)
        realm.isAutoRefresh = true
        realmResults = realm.where(ToDo::class.java).equalTo(ToDo::itemColorName.name, arguments.getString(KEY_ITEM_COLOR_NAME)).findAll()
        //realmResults = realm.where(ToDo::class.java).findAll()
        realmResults.forEach {
            Log.d(this.javaClass.name, it.content)
        }
        adapter      = ToDoAdapter(activity, realmResults)

        VoDolin.observers.addObserver(observer)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view?.findViewById(R.id.textView) as TextView).text = arguments.getString(KEY_ITEM_COLOR_NAME)
        recyclerView = (view?.findViewById(R.id.recycler_view) as RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        VoDolin.observers.deleteObserver(observer)
        realm.close()
    }
}