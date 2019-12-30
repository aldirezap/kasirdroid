package id.co.bigtek.kasir.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import id.co.bigtek.kasir.fragments.FragmentHomeProduk
import id.co.bigtek.kasir.fragments.FragmentHomeTransaksi

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
