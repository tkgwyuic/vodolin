package com.tunesworks.vodolin.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Toast
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
    var isEdited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Realm
        realm = Realm.getInstance(this).apply {
            if (isInTransaction) cancelTransaction()
            beginTransaction()
        }

        // Set realm object
        todo = realm.where(ToDo::class.java)
                .equalTo(ToDo::uuid.name, intent.getStringExtra(KEY_UUID) ?: "")
                .findFirst() ?: return finishWithToast("ToDo Not Found...")

        // Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // StatusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor = todo.itemColor.primaryDark

        // On close icon click
        close.setOnClickListener { onBackPressed() }

        // On done icon click
        done.setOnClickListener {
            if (realm.isInTransaction) {
                // Set values
                todo.updatedAt = Date()
                todo.content   = content.text.toString()
                todo.memo      = memo.text.toString()

                // Commit
                realm.commitTransaction()

                // Notify
                VoDolin.observers.apply {
                    setChanged()
                    notifyObservers(ListFragment.ChangeToDoEvent(todo.itemColorName))
                }

                finishWithToast("Modified.")
            } else finishWithToast("Error! Not modified.") // Realm is not in transaction
        }

        // On delete icon click
        delete.setOnClickListener {
            // Show confirm dialog
            AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Delete the this ToDo?")
                    .setPositiveButton("YES", { dialog, witch ->
                        if (realm.isInTransaction) {
                            // Save color name
                            var itemColorName = todo.itemColorName

                            // Remove & Commit
                            todo.removeFromRealm()
                            realm.commitTransaction()

                            // Notify
                            VoDolin.observers.apply {
                                setChanged()
                                notifyObservers(ListFragment.ChangeToDoEvent(itemColorName))
                            }

                            finishWithToast("Deleted.")
                        } else finishWithToast("Error! Not deleted.") // Realm is not in transaction
                    })
                    .setNegativeButton("Cancel", null)
                    .show()
        }

        // Set text
        content.setText(todo.content)
        memo.setText(todo.memo)

        // Set item label icon and color
        item_label.apply {
            text       = todo.ionicons.icon
            background = GradientDrawable().apply {
                setColor(todo.itemColor.color)
                setShape(GradientDrawable.OVAL)
            }
        }

        // Created at & Updated at
        created_at.text = todo.createdAt.format()
        updated_at.text = todo.updatedAt.format()
    }

    override fun onDestroy() {
        // Realm
        if (realm.isInTransaction) realm.cancelTransaction()
        if (!realm.isClosed) realm.close()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (todo.content != content.text.toString() || todo.memo != memo.text.toString()) {
            // Show confirm dialog
            AlertDialog.Builder(this)
                    .setTitle("Warning!")
                    .setMessage("It has been changed!\nDiscard the changes?")
                    .setPositiveButton("YES", { dialog, witch ->
                        if (realm.isInTransaction) realm.cancelTransaction()
                        super.onBackPressed()
                    })
                    .setNegativeButton("Cancel", null)
                    .show()
        } else {
            if (realm.isInTransaction) realm.cancelTransaction()
            super.onBackPressed()
        }
    }

    private fun finishWithToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        finish()
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