package com.tunesworks.vodolin.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.otto.Subscribe
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.SnackbarCallback
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.activity.DetailActivity
import com.tunesworks.vodolin.event.ItemSelectionChangeEvent
import com.tunesworks.vodolin.event.ToDoEvent
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.itemColor
import com.tunesworks.vodolin.model.status
import com.tunesworks.vodolin.recyclerView.SwipeCallback
import com.tunesworks.vodolin.recyclerView.ToDoAdapter
import com.tunesworks.vodolin.recyclerView.ToDoItemTouchHelper
import com.tunesworks.vodolin.value.Ionicons
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.value.ToDoStatus
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.*
import kotlin.properties.Delegates

class ListFragment: BaseFragment() {
    companion object {
        val KEY_ITEM_COLOR_NAME = "KEY_ITEM_COLOR_NAME"

        fun newInstance(itemColor: ItemColor?): Fragment {
            val args = Bundle().apply {
                putString(KEY_ITEM_COLOR_NAME, itemColor.toString())
            }

            return ListFragment().apply { arguments = args }
        }
    }

    // Subscriber
    @Subscribe fun changeAllToDo(event: ToDoEvent.ChangeAll) {
        if (event.itemColorname == itemColor.toString()) todoAdapter.notifyDataSetChanged()
    }
    @Subscribe fun updateToDo(event: ToDoEvent.Update) {
        if (event.todo.itemColor == itemColor) {
            todoAdapter.notifyDataSetChanged()
        }
    }
    @Subscribe fun moveToDo(event: ToDoEvent.Change) {
        if (event.newTodo.itemColor == itemColor) {
            realmResults.forEachIndexed { i, todo ->
                if (todo.uuid == event.newTodo.uuid) {
                    todoAdapter.notifyItemInserted(i)
                    return
                }
            }
        }
        if (event.oldTodo.itemColor == itemColor) {
            todoAdapter.notifyDataSetChanged()
        }
    }

    var realm:        Realm by Delegates.notNull<Realm>()
    var realmResults: RealmResults<ToDo> by Delegates.notNull<RealmResults<ToDo>>()
    var todoAdapter:  ToDoAdapter by Delegates.notNull<ToDoAdapter>()
    val handler = Handler()
    var itemColor: ItemColor by Delegates.notNull<ItemColor>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Register EventBus
        VoDolin.bus.register(this)

        return inflater?.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemColor = ItemColor.valueOf(arguments.getString(KEY_ITEM_COLOR_NAME))

        // Realm init
        realm        = Realm.getInstance(activity)
        realmResults = realm.where(ToDo::class.java)
                .equalTo(ToDo::itemColorName.name, itemColor.toString())
                .equalTo(ToDo::statusName.name, ToDoStatus.INCOMPLETE.toString())
                .findAllSorted(ToDo::createdAt.name, Sort.DESCENDING)


        // Create recycler view adapter
        todoAdapter = ToDoAdapter(activity, realmResults, object : ToDoAdapter.ViewHolder.ItemListener {
            override fun onItemSelect(holder: ToDoAdapter.ViewHolder, position: Int) {
                todoAdapter.toggleSelection(position)
                VoDolin.bus.post(ItemSelectionChangeEvent(todoAdapter.getSelectedItemCount(), itemColor))
            }

            override fun onItemClick(holder: ToDoAdapter.ViewHolder, position: Int) {
                if (todoAdapter.getSelectedItemCount() > 0) { // Selected some item
                    todoAdapter.toggleSelection(position)
                    VoDolin.bus.post(ItemSelectionChangeEvent(todoAdapter.getSelectedItemCount(), itemColor))
                } else { // Not selected
                    // Start DetailActivity
                    DetailActivity.IntentBuilder
                            .from(activity)
                            .setUUID(realmResults[position].uuid)
                            .start()
                }
            }
        })

        // Create item decoration
        val ith = ToDoItemTouchHelper(object : SwipeCallback(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                var position = viewHolder?.adapterPosition ?: return
                val status   = if (direction == ItemTouchHelper.RIGHT) ToDoStatus.DONE else ToDoStatus.FAILED

                baseActivity.makeSnackbar("Marked as done")?.apply {
                    // Set text
                    when (direction) {
                        ItemTouchHelper.RIGHT -> setText(R.string.done)
                        ItemTouchHelper.LEFT  -> setText(R.string.failed)
                        else -> return
                    }

                    // Set action text
                    setAction("UNDO", {})

                    // Set callback and transaction
                    setCallback(object : SnackbarCallback() {
                        override fun onShown(snackbar: Snackbar?) {
                            // Change value
                            handler.post {
                                if (realm.isInTransaction) realm.cancelTransaction()
                                realm.beginTransaction()
                                realmResults[position].status = status
                                todoAdapter.notifyItemRemoved(position)
                            }
                        }

                        override fun onDismissed(event: Int) {
                            handler.post {
                                if (realm.isInTransaction) {
                                    if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) { // On action button click
                                        // Cancel transaction
                                        realm.cancelTransaction()

                                        // Scroll and Notify
                                        recycler_view.scrollToPosition(position)
                                        todoAdapter.notifyItemInserted(position)
                                    } else { // On dismissed
                                        // Commit transaction
                                        realm.commitTransaction()
                                    }
                                }
                            }
                        }
                    })

                    show()
                }
            }
        }).apply { attachToRecyclerView(recycler_view) }

        // Set LayoutManager, Adapter and ItemDecoration
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity).apply { orientation = LinearLayoutManager.VERTICAL }
            adapter = todoAdapter
            addItemDecoration(ith)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Unregister EventBus
        VoDolin.bus.unregister(this)

        // Close Realm instance
        realm.close()
    }

    fun dismissSelectedItems(func: (ToDo) -> Unit) {
        val selectedItems     = todoAdapter.getSelectedItems()

        baseActivity.makeSnackbar("Marked as Done")?.apply {
            setAction("UNDO", {})
            // Set callback and transaction
            setCallback(object : SnackbarCallback() {
                override fun onShown(snackbar: Snackbar?) {
                    // Change value
                    handler.post {
                        if (realm.isInTransaction) realm.cancelTransaction()
                        realm.beginTransaction()
                        selectedItems.forEach { func.invoke(it) }
                        todoAdapter.notifyDataSetChanged()
                    }
                }

                override fun onDismissed(event: Int) {
                    handler.post {
                        if (realm.isInTransaction) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) { // On action button click
                                // Cancel transaction
                                realm.cancelTransaction()

                                // Notify
                                todoAdapter.notifyDataSetChanged()
                            } else { // On dismissed
                                // Commit transaction
                                realm.commitTransaction()
                            }
                        }
                    }
                }
            })
            show()
        }
    }
}