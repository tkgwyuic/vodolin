package com.tunesworks.vodolin.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
import android.widget.Toast
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.activity.DetailActivity
import com.tunesworks.vodolin.activity.MainActivity
import java.util.*

class AlarmBroadcastReceiver: BroadcastReceiver() {
    companion object {
        val EXTRA_BID     = "EXTRA_BID"
        val EXTRA_UUID    = "EXTRA_UUID"
        val EXTRA_TITLE   = "EXTRA_TITLE"
        val EXTRA_TEXT    = "EXTRA_TEXT"
        val EXTRA_SUBTEXT = "EXTRA_SUBTEXT"
        val EXTRA_WHEN    = "EXTRA_WHEN"
        val EXTRA_INFO    = "EXTRA_INFO"
        val EXTRA_TICKER  = "EXTRA_TICKER"

        val GROUP_KEY = "Deadline Notification"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val uuid = intent?.getStringExtra(EXTRA_UUID) ?: ""
        val bid  = intent?.getIntExtra(EXTRA_BID, 0) ?: 0
        val mainIntent = MainActivity.IntentBuilder(context).setUUID(uuid).build()
        val pendingIntent = PendingIntent.getActivity(context, bid, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = NotificationManagerCompat.from(context)
        val notification = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(intent?.getStringExtra(EXTRA_TITLE) ?: "Your ToDo")
                .setContentText(intent?.getStringExtra(EXTRA_TEXT) ?: "Deadline is approaching")
                .setSubText(intent?.getStringExtra(EXTRA_SUBTEXT) ?: "Tap to Detail")
                .setWhen(intent?.getLongExtra(EXTRA_WHEN, 0) ?: System.currentTimeMillis())
                .setContentInfo(intent?.getStringExtra(EXTRA_INFO) ?: "by VoDolin")
                .setTicker(intent?.getStringExtra(EXTRA_TICKER) ?: "Deadline is approaching")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .build()

        notificationManager.notify(uuid, 0, notification)
    }

    open class IntentBuilder(val context: Context) {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)

        fun setBID(bid: Int)            = putExtra(EXTRA_BID, bid)
        fun setUUID(uuid: String)       = putExtra(EXTRA_UUID, uuid)
        fun setTitle(title: String)     = putExtra(EXTRA_TITLE, title)
        fun setText(text: String)       = putExtra(EXTRA_TEXT, text)
        fun setSubText(subText: String) = putExtra(EXTRA_SUBTEXT, subText)
        fun setInfo(info: String)       = putExtra(EXTRA_INFO, info)
        fun setTicker(ticker: String)   = putExtra(EXTRA_TICKER, ticker)
        fun setWhen(timeMillis: Long)   = putExtra(EXTRA_WHEN, timeMillis)

        fun build() = intent
        fun start() { context.startActivity(intent) }

        private fun putExtra(name: String, extra: Any): IntentBuilder {
            when (extra) {
                is String -> intent.putExtra(name, extra)
                is Int    -> intent.putExtra(name, extra)
                is Long   -> intent.putExtra(name, extra)
            }
            return this
        }
    }
}