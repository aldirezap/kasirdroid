package id.co.bigtek.kasir.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import id.co.bigtek.kasir.fragments.FragmentConfig1
import id.co.bigtek.kasir.fragments.FragmentConfig3

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
