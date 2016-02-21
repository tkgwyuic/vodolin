package com.tunesworks.vodolin.model

import android.text.format.DateFormat
import com.tunesworks.vodolin.value.Ionicons
import com.tunesworks.vodolin.value.ItemColor
import com.tunesworks.vodolin.value.ToDoStatus
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.text.SimpleDateFormat
import java.util.*

open class ToDo(
        @PrimaryKey
        open var uuid: String = UUID.randomUUID().toString(),

        open var content:       String = "",
        open var memo:          String = "",
        open var statusName:    String = ToDoStatus.INCOMPLETE.toString(),
        open var itemColorName: String = ItemColor.DEFAULT.toString(),
        open var ioniconsName:  String = Ionicons.DEFAULT.toString(),
        open var deadline:      Date?  = null,
        open var createdAt:     Date   = Date(),
        open var updatedAt:     Date   = Date()
): RealmObject(){}

var ToDo.itemColor: ItemColor
        get() = ItemColor.valueOf(itemColorName)
        set(value) { itemColorName = value.toString() }

var ToDo.ionicons: Ionicons
        get() = Ionicons.valueOf(ioniconsName)
        set(value) { ioniconsName = value.toString() }

var ToDo.status: ToDoStatus
        get() = ToDoStatus.valueOf(statusName)
        set(value) { statusName = value.toString() }

fun Date.format(fmt: String = "yyyy/MM/dd(E) kk:mm") = DateFormat.format(fmt, this)