package com.tunesworks.vodolin.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.tunesworks.vodolin.value.ItemColor
import kotlinx.android.synthetic.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        // Set Status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.statusBarColor = ItemColor.values()[0].color
    }

    open fun getSnackbarContainer(): CoordinatorLayout? = null

    open fun makeSnackbar(message: String): Snackbar? {
        val container = getSnackbarContainer() ?: return null
        return Snackbar.make(container, message, Snackbar.LENGTH_LONG)
    }
}