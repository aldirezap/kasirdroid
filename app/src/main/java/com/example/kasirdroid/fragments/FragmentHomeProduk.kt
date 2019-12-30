package com.example.kasirdroid.fragments


import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kasirdroid.R
import com.example.kasirdroid.activity.ActivityHome
import com.example.kasirdroid.adapter.list.AdapterListProduk
import com.example.kasirdroid.model.list.ModelListProduk
import com.example.kasirdroid.objek.DBKasir
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds

/**
 * A simple [Fragment] subclass.
 */
class FragmentHomeProduk : Fragment() {
    private var swipe : SwipeRefreshLayout? = null
    private var recyclerView : RecyclerView? = null
    private var produk: ArrayList<ModelListProduk>? = null
    private var animationController : LayoutAnimationController? = null
    private var jmlProduk : TextView?= null
    private var jmlStokProduk : TextView? = null
    private var jmlShowProduk : Int = 10
    private var btnLoadMore : Button? = null
    private var editCari : EditText? = null

    private lateinit var mInterstitialAd: InterstitialAd
    private var adView : AdView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_produk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inisialisasi()
        loadFungsi()
    }

    private fun inisialisasi() {
        val anim : Int = R.anim.layout_animation_from_bottom
        animationController = AnimationUtils.loadLayoutAnimation(activity,anim)
        recyclerView = activity!!.findViewById(R.id.recyclerProduk)
        swipe = activity!!.findViewById(R.id.refreshFragmentProduk)
        jmlProduk = activity!!.findViewById(R.id.txtJumlahTotalProduk)
        btnLoadMore = activity!!.findViewById(R.id.btnLoadMoreProduk)
        jmlStokProduk = activity!!.findViewById(R.id.txtJumlahTotalStokProduk)
        editCari = activity!!.findViewById(R.id.editCariProduk)
    }

    private fun loadFungsi() {
        btnLoadMore!!.setOnClickListener {
            jmlShowProduk +=10
            loadProduk()
        }

        swipe!!.setOnRefreshListener {
            val timer = object: CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    editCari!!.text.clear()
                    jmlShowProduk = 10
                    loadProduk()
                }
            }
            timer.start()
        }

        editCari!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val timer = object : CountDownTimer(1000,500){
                    override fun onFinish() {
                        loadProduk()
                    }

                    override fun onTick(p0: Long) {

                    }

                }
                timer.start()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        loadProduk()
    }

    @SuppressLint("SetTextI18n")
    private fun loadProduk(){
        swipe!!.isRefreshing = false
        produk = ArrayList()
        produk!!.clear()
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.itemAnimator = DefaultItemAnimator()

        var queryJmlProduk = "SELECT COUNT(*) FROM produk"
        var queryJmlStokProduk = "SELECT SUM(produk_stok) FROM produk"
        if(editCari!!.text.isNotEmpty()){
            queryJmlProduk = "SELECT COUNT(*) FROM produk WHERE produk_nama LIKE '%${editCari!!.text}%' OR produk_kode LIKE '%${editCari!!.text}%'"
            queryJmlStokProduk = "SELECT SUM(produk_stok) FROM produk WHERE produk_nama LIKE '%${editCari!!.text}%' OR produk_kode LIKE '%${editCari!!.text}%'"
        }
        val home = activity as ActivityHome

        val layoutNotFoundProduk = activity!!.findViewById<LinearLayout>(R.id.layoutNotFoundProduk)
        if(home.dbHelper.customQuery(queryJmlProduk)!!.getInt(0) > 0){
            layoutNotFoundProduk!!.visibility = View.GONE
        }else{
            layoutNotFoundProduk!!.visibility = View.VISIBLE
        }

        if(home.dbHelper.customQuery(queryJmlProduk)!!.getInt(0) > 10){
            btnLoadMore!!.visibility = View.VISIBLE
        }else{
            btnLoadMore!!.visibility = View.GONE
        }

        jmlProduk!!.text = home.dbHelper.customQuery(queryJmlProduk)!!.getString(0)+" Produk"
        if( home.dbHelper.customQuery( queryJmlStokProduk)!!.getString(0).isNullOrEmpty()){
            jmlStokProduk!!.text = "0 Item"
        }else{
            jmlStokProduk!!.text = home.dbHelper.customQuery( queryJmlStokProduk)!!.getString(0)+" Item"
        }
        var query = "SELECT * FROM produk order by produk_nama ASC LIMIT 0,$jmlShowProduk "
        if(editCari!!.text.isNotEmpty()){
            query = "SELECT * FROM produk WHERE produk_nama LIKE '%${editCari!!.text}%' OR produk_kode LIKE '%${editCari!!.text}%' order by produk_nama ASC LIMIT 0,$jmlShowProduk "
        }
        val cursor : Cursor = home.dbHelper.customQuery(query)!!

        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            val modelProduk = ModelListProduk()
            modelProduk.number = i+1
            modelProduk.kodeProduk = cursor.getString(cursor.getColumnIndex(DBKasir.TabelProduk.PRODUK_KODE))
            modelProduk.namaProduk = cursor.getString(cursor.getColumnIndex(DBKasir.TabelProduk.PRODUK_NAMA))
            modelProduk.hargaPokok = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelProduk.PRODUK_HARGA))
            modelProduk.hargaJual = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelProduk.PRODUK_HARGA_JUAL))
            modelProduk.stok = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelProduk.PRODUK_STOK))
            modelProduk.diskon = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelProduk.PRODUK_DISKON))
            produk!!.add(modelProduk)
            cursor.moveToNext()
        }

        val adapterProduk = AdapterListProduk(activity as ActivityHome, produk!!)
        recyclerView!!.adapter = adapterProduk
    }


    @SuppressLint("HardwareIds")

    override fun onResume() {
        super.onResume()
        loadFungsi()
    }

}
