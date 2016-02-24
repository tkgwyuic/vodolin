package com.tunesworks.vodolin.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.recyclerView.ToDoAdapter
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_search.*
import kotlin.properties.Delegates

class SearchActivity: BaseActivity() {
    var realm: Realm by Delegates.notNull<Realm>()
    var realmResults: RealmResults<ToDo> by Delegates.notNull<RealmResults<ToDo>>()
    var todoAdapter: ToDoAdapter by Delegates.notNull<ToDoAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        realm        = Realm.getInstance(this)
        realmResults = realm.where(ToDo::class.java)
                .findAllSorted(ToDo::createdAt.name, Sort.DESCENDING)

        toolbar.apply {
            title = ""
            inflateMenu(R.menu.search)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { onBackPressed() }
        }

        val searchView = toolbar.menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(s: String?): Boolean {
                return true
            }
        })

        todoAdapter = ToDoAdapter(this, realmResults, object : ToDoAdapter.ViewHolder.ItemListener {
            override fun onItemClick(holder: ToDoAdapter.ViewHolder, position: Int) {
                DetailActivity.IntentBuilder(this@SearchActivity)
                        .setUUID(realmResults[position].uuid)
                        .start()
            }

            override fun onItemSelect(holder: ToDoAdapter.ViewHolder, position: Int) {
            }

        })

        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity).apply { orientation = LinearLayoutManager.VERTICAL }
            adapter = todoAdapter
        }
    }

    open class IntentBuilder(val context: Context) {
        val intent = Intent(context, SearchActivity::class.java)

        fun build() = intent
        fun start() { context.startActivity(intent) }
    }
}