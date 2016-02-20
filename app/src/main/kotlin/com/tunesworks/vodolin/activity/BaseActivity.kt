package com.tunesworks.vodolin.activity

import android.content.Context
import android.content.Intent
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    open fun getSnackbarContainer(): CoordinatorLayout? = null

    open fun makeSnackbar(message: String): Snackbar? {
        val container = getSnackbarContainer() ?: return null
        return Snackbar.make(container, message, Snackbar.LENGTH_LONG)
    }
}