package com.example.kasirdroid.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.kasirdroid.fragments.FragmentConfig1
import com.example.kasirdroid.fragments.FragmentConfig3


class AdapterConfigurasi
constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val pages = listOf(
        FragmentConfig1(),
        FragmentConfig3()
    )

    override fun getItem(position: Int): Fragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return pages.size
    }
}
