package com.tunesworks.vodolin.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.format.DateFormat
import kotlin.properties.Delegates

/**
 * Created by Administrator on 2016/02/24.
 */
class TimePickerDialogFragment(
        val hour: Int,
        val minute: Int
): DialogFragment() {
    var listener: TimePickerDialog.OnTimeSetListener by Delegates.notNull<TimePickerDialog.OnTimeSetListener>()

    override fun onAttach(context: Context?) {
        if (context is TimePickerDialog.OnTimeSetListener) listener = context
        else if (targetFragment is TimePickerDialog.OnTimeSetListener) listener = targetFragment as TimePickerDialog.OnTimeSetListener
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(activity, listener, hour, minute, DateFormat.is24HourFormat(activity))
    }
}