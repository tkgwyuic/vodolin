package com.tunesworks.vodolin

import android.app.Application
import android.util.Log
import com.squareup.otto.Bus
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.util.*

class VoDolin: Application() {
    companion object {
        val bus = Bus()
    }

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GenShinGothic-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
}