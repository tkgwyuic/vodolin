package com.tunesworks.vodolin.fragment

import android.support.v4.app.Fragment
import com.tunesworks.vodolin.activity.BaseActivity

abstract class BaseFragment: Fragment() {
    val baseActivity: BaseActivity
        get() = activity as BaseActivity
}