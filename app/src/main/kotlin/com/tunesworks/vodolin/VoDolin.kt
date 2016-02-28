package com.tunesworks.vodolin

import android.app.Application
import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import com.squareup.otto.Bus
import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.model.ionicons
import com.tunesworks.vodolin.model.itemColor
import com.tunesworks.vodolin.value.Ionicons
import com.tunesworks.vodolin.value.ItemColor
import io.realm.Realm
import io.realm.RealmConfiguration
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.util.*

class VoDolin: Application() {
    companion object {
        val bus = Bus()
    }

    val todoSample = arrayOf(
            ToDo(content = "会長にプルリクエスト", itemColorName = ItemColor.BLUE.toString(), ioniconsName = Ionicons.SOCIAL_OCTCAT.toString()),
            ToDo(content = "社長に伝書鳩", itemColorName = ItemColor.BLUE.toString(), ioniconsName = Ionicons.SOCIAL_TWITTER.toString()),
            ToDo(content = "部長に電話", itemColorName = ItemColor.BLUE.toString(), ioniconsName = Ionicons.CALL.toString()),
            ToDo(content = "課長にメール", itemColorName = ItemColor.BLUE.toString(), ioniconsName = Ionicons.MAIL.toString()),
            ToDo(content = "書類の作成", itemColorName = ItemColor.BLUE.toString(), ioniconsName = Ionicons.DOCUMENT.toString()),
            ToDo(content = "会議の資料作成", itemColorName = ItemColor.BLUE.toString(), ioniconsName = Ionicons.BRIEFCASE.toString()),

            ToDo(content = "録画したプ○キュアを見る", itemColorName = ItemColor.PURPLE.toString(), ioniconsName = Ionicons.HEART.toString()),
            ToDo(content = "発表の資料を作る", itemColorName = ItemColor.PURPLE.toString(), ioniconsName = Ionicons.DOCUMENT.toString()),
            ToDo(content = "一日分の野菜を飲む",      itemColorName = ItemColor.PURPLE.toString(), ioniconsName = Ionicons.WINEGLASS.toString()),

            ToDo(content = "目標をセンターに入れてスイッチ", itemColorName = ItemColor.RED.toString(), ioniconsName = Ionicons.ALERT.toString()),
            ToDo(content = "ガ○ダムによる武力介入", itemColorName = ItemColor.RED.toString(), ioniconsName = Ionicons.FORK.toString()),
            ToDo(content = "海賊王になる", itemColorName = ItemColor.RED.toString(), ioniconsName = Ionicons.PERSON.toString()),
            ToDo(content = "毒も喰らう栄養も喰らう", itemColorName = ItemColor.RED.toString(), ioniconsName = Ionicons.PERSON.toString(),
                    memo = "両方を共に美味いと感じ―――― 血肉に変える度量こそが食には肝要だ"),

            ToDo(content = "スライドの作成", itemColorName = ItemColor.ORANGE.toString(), ioniconsName = Ionicons.SOCIAL_APPLE.toString()),
            ToDo(content = "仕様書の作成", itemColorName = ItemColor.ORANGE.toString(), ioniconsName = Ionicons.DOCUMENT.toString()),

            ToDo(content = "テストコード作成", itemColorName = ItemColor.GREEN.toString(), ioniconsName = Ionicons.BUG.toString()),
            ToDo(content = "CIツール導入", itemColorName = ItemColor.GREEN.toString(), ioniconsName = Ionicons.BUG.toString()),
            ToDo(content = "DIコンテナ導入", itemColorName = ItemColor.GREEN.toString(), ioniconsName = Ionicons.BUG.toString()),
            ToDo(content = "ProGuardによる難読化", itemColorName = ItemColor.GREEN.toString(), ioniconsName = Ionicons.BUG.toString())
    )

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GenShinGothic-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        val config = RealmConfiguration.Builder(applicationContext).build()
        Realm.setDefaultConfiguration(config)

        if (BuildConfig.DEBUG) {
            Realm.deleteRealm(config)
            Realm.getDefaultInstance().use { realm ->
                realm.executeTransaction {
                    todoSample.forEach {
                        realm.copyToRealm(it)
                    }
                }
            }
        }
    }
}