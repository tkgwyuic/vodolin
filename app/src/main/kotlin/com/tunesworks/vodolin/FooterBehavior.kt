package com.tunesworks.vodolin

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

class FooterBehavior(context: Context, attrs: AttributeSet): CoordinatorLayout.Behavior<View>() {
    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        if (dependency != null) {
            val translationY = Math.min(0f, dependency.translationY - dependency.height);
            child?.translationY = translationY
        }

        return true
    }
}