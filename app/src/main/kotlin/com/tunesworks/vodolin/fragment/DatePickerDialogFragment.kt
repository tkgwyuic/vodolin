package com.tunesworks.vodolin.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import kotlin.properties.Delegates

class DatePickerDialogFragment(
        val year: Int,
        val monthOfYear: Int,
        val dayOfMonth: Int
): DialogFragment() {
    var listener: DatePickerDialog.OnDateSetListener by Delegates.notNull<DatePickerDialog.OnDateSetListener>()

    override fun onAttach(context: Context?) {
        if (context is DatePickerDialog.OnDateSetListener) listener = context
        else if (targetFragment is DatePickerDialog.OnDateSetListener) listener = targetFragment as DatePickerDialog.OnDateSetListener
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(activity, listener, year, monthOfYear, dayOfMonth)
    }
}