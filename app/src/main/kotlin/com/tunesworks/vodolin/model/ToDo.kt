package com.tunesworks.vodolin.model

import com.tunesworks.vodolin.ItemColor
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

open class ToDo(
        @PrimaryKey
        open var uuid: String = UUID.randomUUID().toString(),

        open var content:       String = "",
        open var itemColorName: String = ItemColor.DEFAULT.toString(),
        open var deadline:      Date?  = null,
        open var createdAt:     Date   = Date(),
        open var updatedAt:     Date   = Date()
): RealmObject(){}
