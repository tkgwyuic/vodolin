package com.tunesworks.vodolin

import android.support.design.widget.Snackbar

abstract class SnackbarCallback(): Snackbar.Callback() {

    companion object {
        val EVENT_DO   = 0
        val EVENT_UNDO = 1
    }
    var isFirst = true
    override final fun onDismissed(snackbar: Snackbar?, event: Int) {
        if (isFirst) onDismissed(event)
        isFirst = false
    }

    open fun onDismissed(event: Int) {}
}
