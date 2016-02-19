package com.tunesworks.vodolin.fragment

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tunesworks.vodolin.activity.MainActivity
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.SnackbarCallback
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.activity.DetailActivity
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.status
import com.tunesworks.vodolin.recyclerView.ToDoItemTouchHelper
import com.tunesworks.vodolin.recyclerView.SwipeCallback
import com.tunesworks.vodolin.recyclerView.ToDoAdapter
import com.tunesworks.vodolin.value.ToDoStatus
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
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
    var todoAdapter:  ToDoAdapter by Delegates.notNull<ToDoAdapter>()
    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init
        realm        = Realm.getInstance(activity)
        realmResults = realm.where(ToDo::class.java)
                .equalTo(ToDo::itemColorName.name, arguments.getString(KEY_ITEM_COLOR_NAME))
                .equalTo(ToDo::statusName.name, ToDoStatus.INCOMPLETE.toString())
                .findAll()

        // Add observer
        VoDolin.observers.addObserver(observer)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        todoAdapter = ToDoAdapter(activity, realmResults, object : ToDoAdapter.ViewHolder.ItemListener {
            override fun onItemClick(holder: ToDoAdapter.ViewHolder, position: Int) {
                DetailActivity.IntentBuilder.from(activity).setUUID(realmResults[position].uuid).start()
            }
        })


        val ith = ToDoItemTouchHelper(object : SwipeCallback(){
            override fun onRightSwiped(viewHolder: RecyclerView.ViewHolder?, position: Int) {
                Snackbar.make((activity as MainActivity).fab, "Marked as done", Snackbar.LENGTH_LONG).apply {
                    setAction("UNDO", {})
                    setCallback(object : SnackbarCallback() {
                        override fun onShown(snackbar: Snackbar?) {
                            handler.post {
                                if (realm.isInTransaction) realm.cancelTransaction()
                                realm.beginTransaction()
                                realmResults[position].status = ToDoStatus.COMPLETE
                                todoAdapter.notifyItemRemoved(position)
                            }
                        }
                        override fun onDismissed(event: Int) {
                            when (event) {
                                Snackbar.Callback.DISMISS_EVENT_ACTION -> handler.post { // Undo
                                    if (realm.isInTransaction) {
                                        realm.cancelTransaction()
                                        recycler_view.scrollToPosition(position)
                                        todoAdapter.notifyItemInserted(position)
                                    }
                                }

                                else -> handler.post { if (realm.isInTransaction) realm.commitTransaction() }
                            }
                        }
                    })
                }.show()
            }

            override fun onLeftSwiped(viewHolder: RecyclerView.ViewHolder?, position: Int) {
            }
        }).apply { attachToRecyclerView(recycler_view) }

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