package com.tunesworks.vodolin.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.fragment.ListFragment
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.format
import com.tunesworks.vodolin.model.ionicons
import com.tunesworks.vodolin.model.itemColor
import com.tunesworks.vodolin.value.primaryDark
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*
import java.util.*
import kotlin.properties.Delegates

class DetailActivity: BaseActivity() {
    companion object {
        val KEY_UUID = "KEY_UUID"
    }

    var todo: ToDo by Delegates.notNull<ToDo>()
    var realm: Realm by Delegates.notNull<Realm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Realm
        realm = Realm.getInstance(this)
        realm.beginTransaction()
        todo = realm.where(ToDo::class.java)
                .equalTo(ToDo::uuid.name, intent.getStringExtra(KEY_UUID))
                .findFirst()

        // Toolbar and StatusBar
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor = todo.itemColor.primaryDark

        // Close icon
        close.setOnClickListener { onBackPressed() }

        // Done icon
        done.setOnClickListener {
            // Set values
            todo.updatedAt = Date()
            todo.content = content.text.toString()

            // Commit
            if (realm.isInTransaction) realm.commitTransaction()

            // Notify
            VoDolin.observers.apply {
                setChanged()
                notifyObservers(ListFragment.ChangeToDoEvent(todo.itemColorName))
            }

            finish()
        }

        // Delete icon
        delete.setOnClickListener {  }

        // Content edit text
        content.setText(todo.content)

        // Memo edit text
        //memo.setText()

        item_label.background = GradientDrawable().apply {
            setColor(todo.itemColor.color)
            setShape(GradientDrawable.OVAL)
        }
        item_label.text = todo.ionicons.icon

        created_at.text = todo.createdAt.format()
        updated_at.text = todo.updatedAt.format()
    }

    override fun onDestroy() {
        if (realm.isClosed) realm.cancelTransaction()
        if (!realm.isClosed) realm.close()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (realm.isInTransaction) {
            // ToDo: Show confirm dialog
            realm.cancelTransaction()
        }
        super.onBackPressed()
    }

    open class IntentBuilder(val context: Context) {
        companion object {
            fun from(context: Context) = IntentBuilder(context)
        }
        val intent = Intent(context, DetailActivity::class.java)

        fun setUUID(uuid: String): IntentBuilder {
            intent.putExtra(KEY_UUID, uuid)
            return this
        }

        fun start() { context.startActivity(intent) }
    }
}