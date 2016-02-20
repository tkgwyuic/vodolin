package com.tunesworks.vodolin.value

import android.graphics.Color

enum class ItemColor(val color: Int) {
    BLUE   ("#0074bf".parseColor()),
    PURPLE ("#9460a0".parseColor()),
    RED    ("#c93a40".parseColor()),
    ORANGE ("#d16b16".parseColor()),
    GREEN  ("#56a764".parseColor());

    companion object {
        val DEFAULT = BLUE
    }

    fun lighten(fraction: Double): Int {
        return Color.argb(
                Color.alpha(color),
                lightenColor(Color.red(color), fraction),
                lightenColor(Color.green(color), fraction),
                lightenColor(Color.blue(color), fraction)
        )
    }

    fun darken(fraction: Double): Int {
        return Color.argb(
                Color.alpha(color),
                darkenColor(Color.red(color), fraction),
                darkenColor(Color.green(color), fraction),
                darkenColor(Color.blue(color), fraction)
        )
    }

    private fun lightenColor(c: Int, f: Double) = Math.min(c + (c * f), 255.toDouble()).toInt()
    private fun darkenColor(c: Int, f: Double)  = Math.max(c - (c * f), 0.toDouble()).toInt()
}

fun String.parseColor() = Color.parseColor(this)

val ItemColor.primary: Int get() = color
val ItemColor.primaryDark: Int get() = darken(0.2)
val ItemColor.primaryLight: Int get() = lighten(0.2)