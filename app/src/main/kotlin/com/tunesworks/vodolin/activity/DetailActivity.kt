package com.tunesworks.vodolin.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.tunesworks.vodolin.R
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.itemColor
import com.tunesworks.vodolin.value.primary
import com.tunesworks.vodolin.value.primaryDark
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*
import kotlin.properties.Delegates

class DetailActivity: BaseActivity() {
    companion object {
        val KEY_UUID = "KEY_UUID"
    }

    var todo: ToDo by Delegates.notNull<ToDo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        Realm.getInstance(this).use { realm ->
            // Copy stand alone realm object
            todo = realm.copyFromRealm(
                    realm.where(ToDo::class.java)
                            .equalTo(ToDo::uuid.name, intent.getStringExtra(KEY_UUID))
                            .findFirst()
            )
        }
        content.text = todo.content

        appbar.setBackgroundColor(todo.itemColor.primary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor = todo.itemColor.primaryDark
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