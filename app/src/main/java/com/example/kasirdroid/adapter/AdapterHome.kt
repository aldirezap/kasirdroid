package com.example.kasirdroid.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.kasirdroid.fragments.FragmentHomeProduk
import com.example.kasirdroid.fragments.FragmentHomeTransaksi

class AdapterHome
constructor(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val pages = listOf(
        FragmentHomeTransaksi(),
        FragmentHomeProduk()
        //FragmentHomeInformasi()
    )

    override fun getItem(position: Int): Fragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return pages.size
    }
}
