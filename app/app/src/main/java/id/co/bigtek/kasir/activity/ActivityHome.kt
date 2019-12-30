package id.co.bigtek.kasir.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.ui.ScanningActivity
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.adapter.AdapterHome
import id.co.bigtek.kasir.databinding.ActivityHomeBinding
import id.co.bigtek.kasir.helper.DBHelper
import id.co.bigtek.kasir.model.data.ModelHome
import id.co.bigtek.kasir.objek.DBKasir
import id.co.bigtek.kasir.objek.DataClient
import java.text.NumberFormat
import java.util.*


class ActivityHome : AppCompatActivity() {
    lateinit var dbHelper : DBHelper
    private var viewPager : ViewPager? = null
    private var adapterHome : AdapterHome? = null
    private var tabs : TabLayout? = null
    private var btnDashboard : ImageButton? = null
    private var actionAddProduk : FloatingActionButton? = null
    private var binding : ActivityHomeBinding? = null
    private var btnUpdate : ImageButton?= null

    private lateinit var mInterstitialAd: InterstitialAd


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)
        MobileAds.initialize(this,resources.getString(R.string.ad_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = resources.getString(R.string.ad_id_interstitial)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        Printooth.init(this)
        dbHelper = DBHelper(this)
        showData()

        inisialisasi()
        showAds()
    }

    private fun inisialisasi(){
        viewPager = findViewById(R.id.pagerHome)
        adapterHome = AdapterHome(supportFragmentManager)
        tabs = findViewById(R.id.tabs)
        btnDashboard = findViewById(R.id.btnDashboard)
        actionAddProduk = findViewById(R.id.actionAddProduk)

        loadViewPager()
        loadFunction()
    }

    private fun loadViewPager() {
        viewPager!!.adapter = adapterHome
        viewPager!!.offscreenPageLimit = 3
        tabs!!.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            @SuppressLint("RestrictedApi")
            override fun onPageSelected(position: Int) {
                when {
                    viewPager!!.currentItem == 0 -> {
                        actionAddProduk!!.visibility = View.VISIBLE
                    }
                    viewPager!!.currentItem == 1 -> {
                        actionAddProduk!!.visibility = View.VISIBLE
                    }
                    else -> {
                        actionAddProduk!!.visibility = View.GONE
                    }
                }
            }

        })
    }

    private fun loadFunction() {

        actionAddProduk!!.setOnClickListener {
            if(viewPager!!.currentItem == 1){
                val intent = Intent(this@ActivityHome,ActivityAddProduk::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }else{
                val intent = Intent(this@ActivityHome,ActivityTransaksi::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }


        btnDashboard!!.setOnClickListener {
            startActivity(Intent(this@ActivityHome,ActivityDashboard::class.java))
        }
        btnDashboard!!.visibility = View.GONE

    }

    private fun showData() {
        val cache: SharedPreferences? = getSharedPreferences(DataClient.SHARED_PREF_NAME, 0)
        val query2 = "SELECT SUM(detail_transaksi.detail_qty*produk.produk_harga_pokok) FROM detail_transaksi INNER JOIN transaksi on transaksi.transaksi_kode = detail_transaksi.transaksi_kode INNER JOIN produk on detail_transaksi.produk_kode = produk.produk_kode"
        val query1 = "SELECT SUM(detail_transaksi.detail_qty*produk.produk_harga_jual) FROM detail_transaksi INNER JOIN transaksi on transaksi.transaksi_kode = detail_transaksi.transaksi_kode INNER JOIN produk on produk.produk_kode = detail_transaksi.produk_kode"
        val totalPendapatan = if(dbHelper.customQuery(query1)!!.getString(0).isNullOrEmpty()){
            "Rp 0"
        }else{
            formatUang(dbHelper.customQuery(query1)!!.getDouble(0))
        }

        val totalKeuntungan = if(dbHelper.customQuery(query2)!!.getString(0).isNullOrEmpty()){
            "Rp 0"
        }else{
            val a = dbHelper.customQuery(query1)!!.getDouble(0)
            val b = dbHelper.customQuery(query2)!!.getDouble(0)
            val c = a-b
            formatUang(c)
        }

        val home = ModelHome(
            "Smart Kasir Free",
            totalPendapatan,
            totalKeuntungan)
        binding!!.home = home
    }

    private fun formatUang(nilai : Double) : String{
        val localeID = Locale("in","ID")
        val rupiah = NumberFormat.getCurrencyInstance(localeID)
        return rupiah.format(nilai).toString()
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        verifikasi()
        val sql = "DELETE FROM transaksi WHERE transaksi_total = 0"
        dbHelper.customQuery(sql)

        val query = "SELECT * FROM detail_transaksi"
        val cursor : Cursor = dbHelper.customQuery(query)!!

        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            val kodeProduk = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.PRODUK_KODE))
            val kodeDetail = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_KODE))
            val cekKode = "SELECT COUNT(*) FROM produk WHERE produk_kode = '$kodeProduk'"
            if(dbHelper.customQuery(cekKode)!!.getInt(0) == 0){
                dbHelper.customQuery("DELETE FROM detail_transaksi WHERE detail_kode = $kodeDetail")
            }
            cursor.moveToNext()
        }
        showData()
        if(mInterstitialAd.isLoaded){
            mInterstitialAd.show()
        }else{
            showAds()
        }
    }

    @SuppressLint("HardwareIds")
    private fun verifikasi() {
        val cache: SharedPreferences? = getSharedPreferences(DataClient.SHARED_PREF_NAME, 0)
        val deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val savedDeviceID = cache!!.getString(DataClient.CLIENT,"client")

        if(deviceID != savedDeviceID){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pemberitahuan")
            builder.setMessage("Anda sepertinya bukan pengguna resmi dari produk ini.")
            builder.setPositiveButton("Mengerti"){ _: DialogInterface, _: Int ->
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }

            val alertDialog : AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    @SuppressLint("HardwareIds")
    private fun showAds(){
        val request = AdRequest.Builder()
            .build()
        mInterstitialAd.loadAd(request)
        if(mInterstitialAd.isLoaded){
            mInterstitialAd.show()
        }
    }
}
