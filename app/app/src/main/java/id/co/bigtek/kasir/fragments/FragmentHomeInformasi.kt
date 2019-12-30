package id.co.bigtek.kasir.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import id.co.bigtek.kasir.BuildConfig
import id.co.bigtek.kasir.R

/**
 * A simple [Fragment] subclass.
 */
class FragmentHomeInformasi : Fragment() {
    private var swipe : SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_informasi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inisialisasi()
        loadFungsi()
    }

    @SuppressLint("SetTextI18n")
    private fun inisialisasi() {
        val txtVersi = activity!!.findViewById<TextView>(R.id.txtVersi)
        txtVersi!!.text = "Versi "+BuildConfig.VERSION_NAME

        swipe = activity!!.findViewById(R.id.refreshFragmentInformasi)
    }

    private fun loadFungsi() {
        swipe!!.setOnRefreshListener {
            loadInformasi()
        }
    }

    private fun loadInformasi() {
        swipe!!.isRefreshing = false
    }


}
