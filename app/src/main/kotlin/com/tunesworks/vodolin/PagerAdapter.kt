package com.tunesworks.vodolin

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import com.tunesworks.vodolin.fragment.ListFragment

class PagerAdapter(
        val fragmentManager: FragmentManager
): FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int = ItemColor.values().size

    override fun getItem(position: Int): Fragment? {
        return ListFragment.newInstance(ItemColor.values()[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ItemColor.values()[position].toString()
    }
}