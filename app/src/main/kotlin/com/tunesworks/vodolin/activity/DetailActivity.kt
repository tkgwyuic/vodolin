package com.tunesworks.vodolin.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.fragment.ItemLabelDialog
import com.tunesworks.vodolin.fragment.ListFragment
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.format
import com.tunesworks.vodolin.model.ionicons
import com.tunesworks.vodolin.model.itemColor
import com.tunesworks.vodolin.value.Ionicons
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.value.primaryDark
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*
import java.util.*
import kotlin.properties.Delegates

class DetailActivity: BaseActivity(), ItemLabelDialog.OnItemLabelSetListener {
    companion object {
        val KEY_UUID = "KEY_UUID"
    }

    var todo: ToDo by Delegates.notNull<ToDo>()
    var realm: Realm by Delegates.notNull<Realm>()
    var prevItemColorName: String by Delegates.notNull<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Get Realm instance
        realm = Realm.getInstance(this).apply {
            if (isInTransaction) cancelTransaction()
            beginTransaction()
        }

        // Set realm object
        todo = realm.where(ToDo::class.java)
                .equalTo(ToDo::uuid.name, intent.getStringExtra(KEY_UUID) ?: "")
                .findFirst() ?: return finishWithToast("Error: ToDo not found")

        // Save item color name
        prevItemColorName = todo.itemColorName

        // Set Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_close_black_24dp)
            setHomeButtonEnabled(true)
        }

        // Set StatusBar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor = todo.itemColor.primaryDark

        // Set text
        content.setText(todo.content)
        memo.setText(todo.memo)

        // Set item label
        item_label.apply {
            text       = todo.ionicons.icon
            background = GradientDrawable().apply {
                setColor(todo.itemColor.color)
                setShape(GradientDrawable.OVAL)
            }

            // Show dialog
            setOnClickListener { ItemLabelDialog().show(supportFragmentManager, "ItemLabel") }
        }

        // Set Created at and Updated at
        created_at.text = todo.createdAt.format()
        updated_at.text = todo.updatedAt.format()
    }

    override fun onDestroy() {
        // Close Realm
        if (realm.isInTransaction) realm.cancelTransaction()
        if (!realm.isClosed) realm.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            // Discard changes
            android.R.id.home -> onBackPressed()

            // Save changes
            R.id.done -> {
                if (realm.isInTransaction) {
                    // Set values
                    todo.updatedAt = Date()
                    todo.content   = content.text.toString()
                    todo.memo      = memo.text.toString()

                    // Commit
                    realm.commitTransaction()

                    // Notify
                    VoDolin.observers.apply {
                        notifyObservers(ListFragment.ChangeToDoEvent(todo.itemColorName))

                        if (todo.itemColorName != prevItemColorName) {
                            notifyObservers(ListFragment.ChangeToDoEvent(prevItemColorName))
                            notifyObservers(MainActivity.RequestTabScrollEvent(todo.itemColorName))
                        }
                    }

                    finishWithToast("Modified")
                } else finishWithToast("Error! Not modified") // Realm is not in transaction
            }

            // Delete
            R.id.delete -> {
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
                                    notifyObservers(ListFragment.ChangeToDoEvent(itemColorName))
                                }

                                finishWithToast("Deleted.")
                            } else finishWithToast("Error! Not deleted") // Realm is not in transaction
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
            }
            else -> return false
        }
        return true
    }

    override fun onBackPressed() {
        // Check values changes
        if (todo.content != content.text.toString().trim { it == ' ' || it == '　' } ||
                todo.memo != memo.text.toString().trim { it == ' ' || it == '　' } ||
                todo.itemColorName != prevItemColorName) {

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
        } else { // No changes
            if (realm.isInTransaction) realm.cancelTransaction()
            super.onBackPressed()
        }
    }

    override fun onItemLabelSet(itemColor: ItemColor, ionicons: Ionicons) {
        // Set values
        todo.itemColor = itemColor
        todo.ionicons  = ionicons

        // Set item label
        item_label.apply {
            text       = todo.ionicons.icon
            background = GradientDrawable().apply {
                setColor(todo.itemColor.color)
                setShape(GradientDrawable.OVAL)
            }
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