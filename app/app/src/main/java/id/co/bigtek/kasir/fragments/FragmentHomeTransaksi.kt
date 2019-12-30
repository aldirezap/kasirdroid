package id.co.bigtek.kasir.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.activity.ActivityHome
import id.co.bigtek.kasir.activity.ActivityTransaksi
import id.co.bigtek.kasir.adapter.list.AdapterListRiwayatTransaksi
import id.co.bigtek.kasir.model.list.ModelListRiwayatTransaksi
import id.co.bigtek.kasir.objek.DBKasir
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class FragmentHomeTransaksi : Fragment() {
    private var btnTransaksi : ImageButton? = null
    private var jmlTransaksi : TextView? = null
    private var jmlProdukTerjual : TextView? = null
    private var pendapatanHariIni : TextView? = null
    private var keuntunganHariIni : TextView? = null
    private var swipe : SwipeRefreshLayout? = null
    private var recyclerView : RecyclerView? = null
    private var transaksi: ArrayList<ModelListRiwayatTransaksi>? = null
    private var jmlShowProduk : Int = 10
    private var btnLoadMore : Button? = null

    private lateinit var mInterstitialAd: InterstitialAd
    private var adView : AdView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_transaksi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inisialisasi()
        loadFungsi()
    }


    private fun inisialisasi() {
        btnTransaksi = activity!!.findViewById(R.id.btnAddTransaksi)
        jmlTransaksi = activity!!.findViewById(R.id.jmlTransaksiSekarang)
        swipe = activity!!.findViewById(R.id.refreshFragmentTransaksi)
        jmlProdukTerjual = activity!!.findViewById(R.id.txtJumlahProdukTerjualHariIni)
        pendapatanHariIni = activity!!.findViewById(R.id.txtPendapatanHariIni)
        keuntunganHariIni = activity!!.findViewById(R.id.txtKeuntunganHariIni)
        recyclerView = activity!!.findViewById(R.id.recyclerRiwayatTransaksi)
        btnLoadMore = activity!!.findViewById(R.id.btnLoadMoreRiwayatTransaksi)
    }

    private fun loadFungsi() {
        btnLoadMore!!.setOnClickListener {
            jmlShowProduk +=10
            showRiwayatTransaksi()
        }

        btnTransaksi!!.setOnClickListener{
            startActivity(Intent(activity,ActivityTransaksi::class.java))
        }

        swipe!!.setOnRefreshListener {
            jmlShowProduk = 10
            loadTransaksi()
            setContent()
            showRiwayatTransaksi()
        }

        setContent()
    }

    private fun setContent(){
        setWaktu()
        setJmlTransaksi()
        setJumlahPenjualan()
        setPendapatan()
        setKeuntungan()
        showRiwayatTransaksi()
    }

    private fun loadTransaksi() {
        swipe!!.isRefreshing = false
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setJmlTransaksi() {
        val home = activity as ActivityHome?
        val formatTanggal = SimpleDateFormat("dd-M-yyyy")
        val tgl = formatTanggal.format(Date())

        val jml = home!!.dbHelper.customQuery("SELECT COUNT(*) FROM transaksi WHERE transaksi_tanggal = '$tgl'")!!.getString(0)
        jmlTransaksi!!.text = "$jml Transaksi"
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setJumlahPenjualan(){
        val home = activity as ActivityHome?
        val formatTanggal = SimpleDateFormat("dd-M-yyyy")
        val tgl = formatTanggal.format(Date())

        val query =
            "SELECT COUNT(DISTINCT produk_kode) FROM detail_transaksi INNER JOIN transaksi on transaksi.transaksi_kode = detail_transaksi.transaksi_kode WHERE transaksi.transaksi_tanggal = '$tgl'"
        val query2 =
            "SELECT SUM(detail_transaksi.detail_qty) FROM detail_transaksi INNER JOIN transaksi on transaksi.transaksi_kode = detail_transaksi.transaksi_kode WHERE transaksi.transaksi_tanggal = '$tgl'"
        if(home!!.dbHelper.customQuery(query2)!!.getString(0).isNullOrEmpty()){
            jmlProdukTerjual!!.text = home.dbHelper.customQuery(query)!!.getString(0)+" Produk"
            //jmlProdukTerjual!!.text = home.dbHelper.customQuery(query)!!.getString(0)+" Produk (0 item)"
        }else{
            jmlProdukTerjual!!.text = home.dbHelper.customQuery(query)!!.getString(0)+" Produk"
            //jmlProdukTerjual!!.text = home.dbHelper.customQuery(query)!!.getString(0)+" Produk ("+ home.dbHelper.customQuery(query2)!!.getString(0)+" items)"
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setPendapatan(){
        val home = activity as ActivityHome?
        val formatTanggal = SimpleDateFormat("dd-M-yyyy")
        val tgl = formatTanggal.format(Date())

        val query =
            "SELECT SUM(detail_transaksi.detail_qty*produk.produk_harga_jual) FROM detail_transaksi INNER JOIN transaksi on transaksi.transaksi_kode = detail_transaksi.transaksi_kode INNER JOIN produk on produk.produk_kode = detail_transaksi.produk_kode WHERE transaksi.transaksi_tanggal = '$tgl'"
        if(home!!.dbHelper.customQuery(query)!!.getString(0).isNullOrEmpty()){
            pendapatanHariIni!!.text = "Rp 0"
        }else{
            pendapatanHariIni!!.text = ""+ formatUang(home.dbHelper.customQuery(query)!!.getDouble(0))
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setKeuntungan(){
        val home = activity as ActivityHome?
        val formatTanggal = SimpleDateFormat("dd-M-yyyy")
        val tgl = formatTanggal.format(Date())

        val query = "SELECT SUM(detail_transaksi.detail_qty*produk.produk_harga_jual) FROM detail_transaksi INNER JOIN transaksi on transaksi.transaksi_kode = detail_transaksi.transaksi_kode INNER JOIN produk on detail_transaksi.produk_kode = produk.produk_kode WHERE transaksi.transaksi_tanggal = '$tgl'"
        val query2 = "SELECT SUM(detail_transaksi.detail_qty*produk.produk_harga_pokok) FROM detail_transaksi INNER JOIN transaksi on transaksi.transaksi_kode = detail_transaksi.transaksi_kode INNER JOIN produk on detail_transaksi.produk_kode = produk.produk_kode WHERE transaksi.transaksi_tanggal = '$tgl'"
        if(home!!.dbHelper.customQuery(query)!!.getString(0).isNullOrEmpty()){
            keuntunganHariIni!!.text = "Rp 0"
        }else{
            val a = home.dbHelper.customQuery(query)!!.getDouble(0)
            val b = home.dbHelper.customQuery(query2)!!.getDouble(0)
            val c = a-b
            keuntunganHariIni!!.text = formatUang(c)
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setWaktu() {
        val formatTanggal = SimpleDateFormat("dd")
        val formatBulan = SimpleDateFormat("MMM")
        val tgl = formatTanggal.format(Date())
        val bln = formatBulan.format(Date())

        val tanggal = activity!!.findViewById<TextView>(R.id.tgl)
        val bulan = activity!!.findViewById<TextView>(R.id.bulan)

        tanggal!!.text = tgl
        bulan!!.text = bln

        val formatWaktu = SimpleDateFormat("EEEE, dd MMMM yyyy")
        val waktu = formatWaktu.format(Date())
        val txtWaktu = activity!!.findViewById<TextView>(R.id.txtWaktuSekarang)
        txtWaktu!!.text = waktu
    }

    private fun batasiKoma(nilai : BigDecimal): String {
        val df = DecimalFormat("#.##")

        var value : BigDecimal = nilai
        value = df.format(value).toBigDecimal()
        value.setScale(2,RoundingMode.HALF_UP).stripTrailingZeros()
        return value.intValueExact().toString()
    }

    private fun formatUang(nilai : Double) : String{
        val localeID = Locale("in","ID")
        val rupiah = NumberFormat.getCurrencyInstance(localeID)
        return rupiah.format(nilai).toString()
    }

    @SuppressLint("SetTextI18n")
    private fun showRiwayatTransaksi(){
        swipe!!.isRefreshing = false
        transaksi = ArrayList()
        transaksi!!.clear()
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.itemAnimator = DefaultItemAnimator()

        val home = activity as ActivityHome
        val queryJmlTransaksi = "SELECT COUNT(*) FROM transaksi order by transaksi_kode"
        val jumlahSemuaTransaksi = activity!!.findViewById<TextView>(R.id.txtJumlahSemuaTransaksi)
        jumlahSemuaTransaksi!!.text = home.dbHelper.customQuery(queryJmlTransaksi)!!.getInt(0).toString()

        val layoutNotFound = activity!!.findViewById<LinearLayout>(R.id.layoutNotFoundTransaction)
        if(home.dbHelper.customQuery(queryJmlTransaksi)!!.getInt(0) > 0){
            layoutNotFound!!.visibility = View.GONE
        }else{
            layoutNotFound!!.visibility = View.VISIBLE
        }

        if(home.dbHelper.customQuery(queryJmlTransaksi)!!.getInt(0) > 10){
            btnLoadMore!!.visibility = View.VISIBLE
        }else{
            btnLoadMore!!.visibility = View.GONE
        }
        val query = "SELECT * FROM transaksi order by transaksi_kode DESC LIMIT 0,$jmlShowProduk "
        val cursor : Cursor = home.dbHelper.customQuery(query)!!

        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            val modelTransaksi = ModelListRiwayatTransaksi()
            modelTransaksi.kodeTransaksi = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelTransaksi.TRANSAKSI_KODE))
            modelTransaksi.tanggal = cursor.getString(cursor.getColumnIndex(DBKasir.TabelTransaksi.TRANSAKSI_TGL))
            modelTransaksi.subTotal = cursor.getDouble(cursor.getColumnIndex(DBKasir.TabelTransaksi.TRANSAKSI_SUB_TOTAL))
            modelTransaksi.total = cursor.getDouble(cursor.getColumnIndex(DBKasir.TabelTransaksi.TRANSAKSI_TOTAL))
            modelTransaksi.kembalian = cursor.getDouble(cursor.getColumnIndex(DBKasir.TabelTransaksi.TRANSAKSI_KEMBALIAN))
            modelTransaksi.diskon = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelTransaksi.TRANSAKSI_DISKON))
            modelTransaksi.bayar = cursor.getDouble(cursor.getColumnIndex(DBKasir.TabelTransaksi.TRANSAKSI_JML_BAYAR))
            transaksi!!.add(modelTransaksi)
            cursor.moveToNext()
        }

        val adapterTransaksi = AdapterListRiwayatTransaksi(activity as ActivityHome, transaksi!!)
        recyclerView!!.adapter = adapterTransaksi
    }



    @SuppressLint("HardwareIds")

    override fun onResume() {
        super.onResume()
        setContent()
        showRiwayatTransaksi()
    }
}
