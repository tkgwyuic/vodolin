package com.tunesworks.vodolin.customView

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class ItemLabelGridView : GridView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val num = numColumns
        val density = resources.displayMetrics.density
        val px = Math.round(columnWidth * density)

        val count  = adapter?.count ?: 0
        val width  = px * num
        val height = px * Math.ceil(count / num.toDouble())
        setMeasuredDimension(width, height.toInt())
    }
}