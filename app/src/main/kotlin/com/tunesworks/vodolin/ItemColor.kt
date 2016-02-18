package com.tunesworks.vodolin

import android.graphics.Color

enum class ItemColor(val color: Int) {
    BLUE   ("#0074bf".parseColor()),
    ORANGE ("#de9610".parseColor()),
    RED    ("#c93a40".parseColor()),
    YELLOW ("#f2cf01".parseColor()),
    GREEN  ("#56a764".parseColor());

    companion object {
        val DEFAULT = BLUE
    }
}

fun String.parseColor() = Color.parseColor(this)