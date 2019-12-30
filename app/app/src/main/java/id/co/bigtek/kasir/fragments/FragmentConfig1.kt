package id.co.bigtek.kasir.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.activity.ActivityConfigurasi

/**
 * A simple [Fragment] subclass.
 */
class FragmentConfig1 : Fragment() {
    private var btnNext : Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnNext = activity!!.findViewById(R.id.btnNext1)
        btnNext!!.setOnClickListener{
            changeLayout()
        }
    }

    private fun changeLayout(){
        val configurasi = activity as ActivityConfigurasi?
        configurasi!!.changeLayout(configurasi.pagerConfigurasi!!.currentItem+1)
    }


}
