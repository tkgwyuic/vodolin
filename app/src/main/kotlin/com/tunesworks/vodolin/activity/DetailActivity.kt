package com.tunesworks.vodolin.activity

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
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
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.VoDolin
import com.tunesworks.vodolin.fragment.DatePickerDialogFragment
import com.tunesworks.vodolin.fragment.ItemLabelDialog
import com.tunesworks.vodolin.fragment.ListFragment
import com.tunesworks.vodolin.fragment.TimePickerDialogFragment
import com.tunesworks.vodolin.model.*
import com.tunesworks.vodolin.receiver.AlarmBroadcastReceiver
import com.tunesworks.vodolin.value.Ionicons
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.value.primaryDark
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*
import java.util.*
import kotlin.properties.Delegates

class DetailActivity: BaseActivity(), ItemLabelDialog.OnItemLabelSetListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{
    companion object {
        val KEY_UUID = "KEY_UUID"
    }

    var todo: ToDo by Delegates.notNull<ToDo>()
    var realm: Realm by Delegates.notNull<Realm>()
    var prevItemColorName: String by Delegates.notNull<String>()

    var date: Date by Delegates.notNull<Date>()

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

        switch_notify.apply {
            val bid = todo.createdAt.time.toInt()
            val am = getSystemService(ALARM_SERVICE) as AlarmManager

            setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    //val calendar = Calendar.getInstance()
                    //calendar.timeInMillis = System.currentTimeMillis()
                    var deadlineMillis = todo.deadline?.time ?: Date().time

                    val intent = AlarmBroadcastReceiver.IntentBuilder(applicationContext)
                            .setBID(bid)
                            .setUUID(todo.uuid)
                            .setTitle(todo.content)
                            .setWhen(deadlineMillis)
                            .setTicker(todo.content)
                            .build()

                    val pending = PendingIntent.getBroadcast(applicationContext, bid, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        am.setExact(AlarmManager.RTC_WAKEUP, deadlineMillis, pending)
                    } else {
                        am.set(AlarmManager.RTC_WAKEUP, deadlineMillis, pending)
                    }

                    Toast.makeText(applicationContext, "Set Alarm ", Toast.LENGTH_SHORT).show()
                } else {
                    val intent  = Intent(applicationContext, AlarmBroadcastReceiver::class.java)
                    val pending = PendingIntent.getBroadcast(applicationContext, bid, intent, 0)

                    am.cancel(pending)
                }
            }
        }

        date = todo.deadline ?: Date()
        var year   = date.format("yyyy").toInt()
        var month  = date.format("MM").toInt()
        var day    = date.format("dd").toInt()
        var hour   = date.format("HH").toInt()
        var minute = date.format("mm").toInt()

        deadline_date.apply {
            text = todo.deadline?.format("yyyy/MM/dd (E)") ?: "Tap to set deadline."
            setOnClickListener {
                DatePickerDialogFragment(year, month, day)
                        .show(supportFragmentManager, "Date Picker")
            }
        }

        deadline_time.apply {
            if (todo.deadline == null)visibility = View.GONE
            setOnClickListener {
                TimePickerDialogFragment(hour, minute)
                        .show(supportFragmentManager, "Time Picker")
            }
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

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        var y  = year.format("%02d")
        var m  = (monthOfYear+1).format("%02d")
        var d  = dayOfMonth.format("%02d")
        date = "$y $m $d ${date.format("HH")} ${date.format("mm")}".parseDate("yyyy MM dd HH mm")

        todo.deadline = date
        deadline_date.text = todo.deadline?.format("yyyy/MM/dd (E)")
        deadline_time.text = todo.deadline?.format("HH:mm")
        deadline_time.visibility = View.VISIBLE
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        var h = hour.format("%02d")
        var m = minute.format("%02d")
        date = "${date.format("yyyy")} ${date.format("MM")} ${date.format("dd")} $h $m".parseDate("yyyy MM dd HH mm")

        todo.deadline = date
        deadline_time.text = todo.deadline?.format("HH:mm")

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

        fun build() = intent
        fun start() { context.startActivity(intent) }
    }
}