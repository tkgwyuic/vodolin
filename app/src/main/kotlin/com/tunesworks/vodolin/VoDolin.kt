package com.tunesworks.vodolin

import android.app.Application
import android.util.Log
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.util.*

class VoDolin: Application() {
    companion object {
        val observers = VoDolinObservable()
    }

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GenShinGothic-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }

    class VoDolinObservable : Observable() {
        override public fun setChanged() {
            super.setChanged()
        }

        override fun notifyObservers(data: Any?) {
            setChanged()
            super.notifyObservers(data)
            Log.d(this.javaClass.name, "notifyObservers")
        }
    }
}