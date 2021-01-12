package com.asusoft.calendar.fragment.month

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter

@SuppressLint("WrongConstant")
class MonthPageAdapter(val context: Context, fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val numberOfPages = 3

    override fun getCount(): Int {
        return numberOfPages + 2
    }

    override fun getItem(position: Int): Fragment {

    }
}