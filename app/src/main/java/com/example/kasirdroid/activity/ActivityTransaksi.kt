package com.example.kasirdroid.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kasirdroid.R
import com.example.kasirdroid.adapter.list.AdapterListTransaksi
import com.example.kasirdroid.adapter.list.AdapterResultSearchProduk
import com.example.kasirdroid.helper.DBHelper
import com.example.kasirdroid.model.database.ModelTransaksi
import com.example.kasirdroid.model.list.ModelListTransaksi
import com.example.kasirdroid.model.list.ModelResultSearchProduk
import com.example.kasirdroid.objek.DBKasir
import com.example.kasirdroid.objek.DataClient
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_transaksi.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ActivityTransaksi : AppCompatActivity() {
    lateinit var dbHelper : DBHelper


    private var btnBayar : Button? = null

    private var btnBack : ImageButton? = null
    private var btnCamera : ImageButton? = null
    private var btnClear : ImageButton? = null

    private var txtCari : EditText? = null
    private var txtCash : EditText? = null
    private var txtDiskon : EditText? = null

    private var txtTotalBayar : TextView? = null
    private var txtJumlahProduk : TextView? = null

    private var layoutHasilPencarian : NestedScrollView? = null
    private var recyclerListTransaksi : RecyclerView? = null

    private var produk: ArrayList<ModelResultSearchProduk>? = null
    private var produkTransaki: ArrayList<ModelListTransaksi>? = null
    private var listHasilPncarian : RecyclerView? = null

    private var jmlItem = 0
    private var jmlProduk = 0
    private var jmlTotal = 0.0
    private var jmlSubTotal = 0.0
    private var jmlBayar = 0.0
    private var jmlKembalian = 0.0
    private var jmlDiskon = 0
    var idTransaksi = 0

    private var bluetoothAdapter : BluetoothAdapter? = null
    private var enableBT : Int = 1

    private var camera : Boolean = false
    private var showPencarian : Int = 10
    private var btnShowMore : Button? = null

    private lateinit var mInterstitialAd: InterstitialAd
    private var adView : AdView? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            window.statusBarColor = Color.WHITE
        }
        dbHelper = DBHelper(this)
        MobileAds.initialize(this,resources.getString(R.string.ad_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = resources.getString(R.string.ad_id_interstitial)
        val request = AdRequest.Builder()
            .build()
        mInterstitialAd.loadAd(request)

        askPermissions()
        inisialisasi()
        siapkanDataTransaksi()
    }

    private fun inisialisasi() {
        produkTransaki = ArrayList()
        layoutHasilPencarian = findViewById(R.id.layoutHasilPencarian)
        recyclerListTransaksi = findViewById(R.id.listTransaksi)
        listHasilPncarian = findViewById(R.id.listHasilPencarian)
        btnBack = findViewById(R.id.btnBack)
        txtCari = findViewById(R.id.editCari)
        txtCash = findViewById(R.id.editTextCash)
        txtDiskon = findViewById(R.id.editTextGiveDiskon)
        txtTotalBayar = findViewById(R.id.txtTotalBayar)
        txtJumlahProduk = findViewById(R.id.txtJumlahProduk)
        btnClear = findViewById(R.id.btnClear)
        btnBayar = findViewById(R.id.btnBayar)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        btnShowMore = findViewById(R.id.btnLoadMoreProdukCari)

        loadFunction()
    }

    private fun loadFunction(){
        btnShowMore!!.setOnClickListener {
            if(txtCari!!.text.toString().isNotEmpty()){
                showPencarian +=10
                cari(txtCari!!.text.toString())
            }
        }

        btnClear!!.setOnClickListener {
            if(jmlItem == 0){
                resetTransaksi()
                siapkanDataTransaksi()
                txtCari!!.setText("")
                txtCari!!.clearFocus()
                layoutHasilPencarian!!.visibility = View.GONE
                listTransaksi()
                Toast.makeText(this, "Memulai transaksi baru.",Toast.LENGTH_SHORT).show()
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Apakah Anda yakin akan mengulang transaksi? Data yang ada akan dihapus.")
                builder.setPositiveButton("Ya"){ _: DialogInterface, _: Int ->
                    resetTransaksi()
                    siapkanDataTransaksi()
                    txtCari!!.setText("")
                    txtCari!!.clearFocus()
                    layoutHasilPencarian!!.visibility = View.GONE
                    listTransaksi()
                    Toast.makeText(this, "Memulai transaksi baru.",Toast.LENGTH_SHORT).show()
                }

                builder.setNegativeButton("Tidak"){ _: DialogInterface, _: Int ->

                }

                val alertDialog : AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }

        btnBayar!!.setOnClickListener {
            if(txtDiskon!!.text.isNotEmpty()){
                if(Integer.valueOf(txtDiskon!!.text.toString()) <= 100){
                    jmlDiskon = Integer.valueOf(txtDiskon!!.text.toString())
                }else{
                    Toast.makeText(this,"Diskon melebihi batas maksimal.",Toast.LENGTH_SHORT).show()
                }
            }else{
                jmlDiskon = Integer.valueOf("0")
            }

            if(jmlTotal == 0.0 || jmlItem == 0){
                Toast.makeText(this,"Daftar transaksi kosong.",Toast.LENGTH_SHORT).show()
            }else{
                if(txtCash!!.text.isEmpty()){
                    Toast.makeText(this,"Harap masukan jumlah cash.",Toast.LENGTH_SHORT).show()
                }else{
                    jmlBayar = txtCash!!.text.toString().toDouble()
                    if(jmlBayar < jmlTotal){
                        Toast.makeText(this,"Cash masih kurang dari total belanja.",Toast.LENGTH_SHORT).show()
                    }else{
                        val potongan = (txtDiskon!!.text.toString().toDouble()/100)* jmlTotal
                        jmlSubTotal = jmlTotal-potongan
                        jmlKembalian = jmlBayar-jmlSubTotal

                        updateDataTransaksi()
                        updateStok()
                        showKembalian()
                        /*val timer = object : CountDownTimer(2000,1000){
                            override fun onTick(p0: Long) {

                            }

                            override fun onFinish() {

                            }
                        }
                        timer.start()*/
                    }
                }
            }
        }

        btnBack!!.setOnClickListener{
            back()
        }

        val btnBack2 = findViewById<ImageButton>(R.id.btnBack2)
        btnBack2!!.setOnClickListener {
            back()
        }

        txtCari!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                showPencarian = 10
                cari(txtCari!!.text.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(txtCari!!.text.toString().isEmpty()){
                    btnShowMore!!.visibility = View.GONE
                }else{
                    btnShowMore!!.visibility = View.VISIBLE
                }
                showOrHideResult()
            }

        })

    }


    @SuppressLint("SetTextI18n")
    private fun showKembalian(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.content_alert_kembalian,null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        mDialogView.findViewById<TextView>(R.id.txtKembalianBelanja).text = formatUang(jmlKembalian)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)

        mDialogView.findViewById<Button>(R.id.btnBuatTransaksiBaru).setOnClickListener {
            siapkanDataTransaksi()
            txtCari!!.setText("")
            txtCari!!.clearFocus()
            txtCash!!.setText("")
            txtCash!!.clearFocus()
            txtDiskon!!.setText("")
            txtDiskon!!.clearFocus()
            layoutHasilPencarian!!.visibility = View.GONE
            listTransaksi()

            mAlertDialog.dismiss()
        }
    }

    private fun showOrHideResult(){
        if(txtCari!!.isFocused){
            layoutHasilPencarian!!.visibility = View.VISIBLE
        }else{
            layoutHasilPencarian!!.visibility = View.GONE
        }
    }

    private fun cari(data : String){
        produk = ArrayList()
        produk!!.clear()
        listHasilPncarian!!.layoutManager = LinearLayoutManager(this)
        listHasilPncarian!!.itemAnimator = DefaultItemAnimator()

        val jmlHasil = dbHelper.customQuery("SELECT COUNT(*) FROM produk WHERE produk_nama LIKE '%$data%' OR produk_kode LIKE '$data%'")!!.getInt(0)
        if(jmlHasil > 10){
            btnLoadMoreProdukCari!!.visibility = View.VISIBLE
        }else{
            btnLoadMoreProdukCari!!.visibility = View.GONE
        }
        val query = "SELECT * FROM produk WHERE produk_nama LIKE '%$data%' OR produk_kode LIKE '$data%' LIMIT 0,$showPencarian"
        val cursor : Cursor = dbHelper.customQuery(query)!!

        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            val modelProduk = ModelResultSearchProduk()
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

        val adapterProduk = AdapterResultSearchProduk(this, produk!!)
        listHasilPncarian!!.adapter = adapterProduk
    }

    @SuppressLint("SetTextI18n")
    fun listTransaksi(){
        txtTotalBayar!!.text = "Rp0"
        produkTransaki!!.clear()
        recyclerListTransaksi!!.layoutManager = LinearLayoutManager(this)
        recyclerListTransaksi!!.itemAnimator = DefaultItemAnimator()
        recyclerListTransaksi!!.removeAllViewsInLayout()

        val query = "SELECT * FROM detail_transaksi WHERE transaksi_kode = $idTransaksi"
        val cursor : Cursor = dbHelper.customQuery(query)!!

        var total = 0.0
        var item = 0
        jmlProduk = 0

        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            val modelProduk = ModelListTransaksi()
            modelProduk.kodeDetailTransaksi = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_KODE))
            modelProduk.kodeProduk = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.PRODUK_KODE))
            modelProduk.kodeTransaksi = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.TRANSAKSI_KODE))
            modelProduk.qty = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_QTY))

            val getHargaProduk = "SELECT produk_harga_jual FROM produk WHERE produk_kode = '"+cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.PRODUK_KODE))+"'"
            val harga = dbHelper.customQuery(getHargaProduk)!!.getDouble(0)

            val getDiskonProduk = "SELECT produk_diskon FROM produk WHERE produk_kode = '"+cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.PRODUK_KODE))+"'"
            val diskon = dbHelper.customQuery(getDiskonProduk)!!.getInt(0)

            val qty = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_QTY))
            var hargaAkhir = harga*qty
            var jumlahPotongan = diskon*hargaAkhir
            jumlahPotongan /= 100
            hargaAkhir -= jumlahPotongan

            total += hargaAkhir
            jmlTotal = total

            val jmlItems = "SELECT SUM(detail_qty) FROM detail_transaksi WHERE transaksi_kode = "+cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.TRANSAKSI_KODE))
            item += dbHelper.customQuery(jmlItems)!!.getInt(0)

            jmlItem = item
            jmlProduk += 1
            produkTransaki!!.add(modelProduk)
            cursor.moveToNext()
        }

        val adapterTransaksi = AdapterListTransaksi(this, produkTransaki!!)
        recyclerListTransaksi!!.adapter = adapterTransaksi
        txtTotalBayar!!.text = formatUang(jmlTotal)
        txtJumlahProduk!!.text = "($jmlProduk Item)"
        recyclerListTransaksi!!.adapter!!.notifyDataSetChanged()
        adapterTransaksi.notifyDataSetChanged()

        updateDataTransaksi()
    }

    private fun updateDataTransaksi(){
        val query = "UPDATE transaksi set transaksi_bayar = $jmlBayar"+
                ", transaksi_total = $jmlTotal"+
                ", transaksi_sub_total = $jmlSubTotal"+
                ", transaksi_kembalian = $jmlKembalian"+
                ", transaksi_diskon = $jmlDiskon"+
                " WHERE transaksi_kode = $idTransaksi"
        dbHelper.customQuery(query)
    }

    private fun updateStok(){
        val query = "SELECT * FROM detail_transaksi WHERE transaksi_kode = $idTransaksi"
        val cursor : Cursor = dbHelper.customQuery(query)!!
        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            val kodeProduk = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.PRODUK_KODE))
            val qty = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_QTY))

            val updateStokProduk = "UPDATE produk SET produk_stok = produk_stok-$qty WHERE produk_kode = '$kodeProduk'"
            dbHelper.customQuery(updateStokProduk)
            cursor.moveToNext()
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun siapkanDataTransaksi(){
        jmlItem = 0
        jmlBayar = 0.0
        jmlTotal = 0.0
        jmlSubTotal = 0.0
        jmlKembalian = 0.0
        jmlProduk = 0
        jmlDiskon = 0
        txtTotalBayar!!.text = "Rp 0"

        val formatTanggal = SimpleDateFormat("dd-M-yyyy")
        val tgl = formatTanggal.format(Date())

        val query = "SELECT COUNT(*) FROM transaksi"
        val query2 = "Select MAX(transaksi_kode) FROM transaksi"
        idTransaksi = if(dbHelper.customQuery(query)!!.getInt(0) == 0){
            1
        }else{
            dbHelper.customQuery(query2)!!.getInt(0)+1
        }

        if(dbHelper.insertTransaksi(
                ModelTransaksi(
                    kode = idTransaksi,
                    tanggal = tgl,
                    total = jmlTotal,
                    subtotal = jmlSubTotal,
                    jml_bayar = jmlBayar,
                    jml_kembalian = jmlKembalian,
                    diskon = jmlDiskon
                )
            )){
            txtCari!!.setText("")
            txtCari!!.clearFocus()
        }
    }



    override fun onBackPressed() {
        if(txtCari!!.isFocused){
            txtCari!!.clearFocus()
            showOrHideResult()
        }else{
            if(layoutHasilPencarian!!.isVisible){
                showOrHideResult()
            }else{
                confirmExit()
            }
        }
    }

    private fun confirmExit(){
        if(jmlItem == 0){
            resetTransaksi()
            finish()
        }else{
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Apakah Anda yakin akan meninggalkan halaman transaksi? Data transaksi ini akan dibersihkan.")
            builder.setPositiveButton("Ya"){ _: DialogInterface, _: Int ->
                resetTransaksi()
                finish()
            }

            builder.setNegativeButton("Batal"){ _: DialogInterface, _: Int ->

            }

            val alertDialog : AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    private fun back(){
        if(txtCari!!.isFocused){
            txtCari!!.clearFocus()
            showOrHideResult()
        }else{
            if(layoutHasilPencarian!!.isVisible){
                showOrHideResult()
            }else{
                confirmExit()
            }
        }
    }

    private fun resetTransaksi(){
        val sql1 = "DELETE FROM "+ DBKasir.TabelTransaksi.NAMA_TABEL+" WHERE "+DBKasir.TabelTransaksi.TRANSAKSI_KODE+" = "+idTransaksi
        val sql2 = "DELETE FROM "+ DBKasir.TabelDetailTransaksi.NAMA_TABEL+" WHERE "+DBKasir.TabelDetailTransaksi.TRANSAKSI_KODE+" = "+idTransaksi
        dbHelper.customQuery(sql1)
        dbHelper.customQuery(sql2)
    }

    private fun askPermissions(){
        val requestCode = 232
        val permissions =
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, permissions, requestCode)
            }
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun buatStruk(){
        val cache: SharedPreferences? = getSharedPreferences(DataClient.SHARED_PREF_NAME, 0)
        val formatTanggal = SimpleDateFormat("dd-M-yyyy")
        val tanggal = formatTanggal.format(Date())
        val toko = cache!!.getString(DataClient.TOKO,"toko")
        val alamat = cache.getString(DataClient.ALAMAT,"alamat")
        val telp = cache.getString(DataClient.TELP,"telp")
        //val kasir = cache.getString(DataClient.NAMA,"nama")
        val noTransaksi = idTransaksi

        var textToPrint: String =
            "<CENTER><MEDIUM1>$toko<BR>" +
                    "<CENTER><NORMAL>$alamat<BR>" +
                    "<CENTER><NORMAL>$telp<BR><BR>" +
                    "<LEFT><NORMAL>No. <RIGHT><NORMAL>$noTransaksi<BR>"+
                    "<LEFT><NORMAL>Tanggal <RIGHT><NORMAL>$tanggal<BR><LINE>"

        val getData = "SELECT * FROM detail_transaksi WHERE transaksi_kode = $noTransaksi"
        val cursor : Cursor = dbHelper.customQuery(getData)!!
        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            val qtyBeli = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_QTY))
            var diskonProduk: Int
            val writeDiskon: String

            val kodeProduk = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.PRODUK_KODE))
            val getDiskon = "SELECT produk_diskon FROM produk WHERE produk_kode = '$kodeProduk'"
            diskonProduk = dbHelper.customQuery(getDiskon)!!.getInt(0)
            writeDiskon = if(diskonProduk > 0){
                "(-$diskonProduk%)"
            }else{
                ""
            }

            val getNamaProduk = "SELECT produk_nama FROM produk WHERE produk_kode = '$kodeProduk'"
            val namaProduk = dbHelper.customQuery(getNamaProduk)!!.getString(0)

            val getHargaJual = "SELECT produk_harga_jual FROM produk WHERE produk_kode = '$kodeProduk'"
            val hargaJual = dbHelper.customQuery(getHargaJual)!!.getDouble(0)
            val totalBeliProduk = hargaJual.toBigDecimal()*qtyBeli.toBigDecimal()

            textToPrint +=
                "<NORMAL>$namaProduk <RIGHT><NORMAL>${formatUang(totalBeliProduk.toDouble())}<BR>" +
                        "<NORMAL>$qtyBeli x ${formatUang(hargaJual)} $writeDiskon<BR>"

            cursor.moveToNext()
        }

        textToPrint +=
            "<LINE><BR>" +
                    "<LEFT><NORMAL>Item : $jmlItem<BR>" +
                    "<LEFT><NORMAL>Qty  : $jmlProduk<BR>" +
                    "<LEFT><NORMAL>Total <RIGHT><NORMAL>${formatUang(jmlTotal)}<BR>" +
                    "<RIGHT><NORMAL>-${batasiKoma(jmlDiskon.toBigDecimal())}%<BR>" +
                    "<LEFT><NORMAL>Sub Total <RIGHT><NORMAL>${formatUang(jmlSubTotal)}<BR>" +
                    "<LEFt><NORMAL>Bayar (CASH) <RIGHT><NORMAL>${formatUang(jmlBayar)}<BR>" +
                    "<LEFT><NORMAL>Kembali <RIGHT><NORMAL>${formatUang(jmlKembalian)}<BR><BR>" +
                    "<CENTER><NORMAL>-Terimakasih-<BR>" +
                    "<CENTER><NORMAL>Powered by BIGTEK<BR>" +
                    "<CENTER><NORMAL>www.bigtek.co.id"

        val intent =  Intent("pe.diegoveloper.printing")
        //intent.setAction(android.content.Intent.ACTION_SEND);
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT,textToPrint)
        startActivity(intent)
    }

    @SuppressLint("SimpleDateFormat")


    private fun batasiKoma(nilai : BigDecimal): String {
        val df = DecimalFormat("#.##")

        var value : BigDecimal = nilai
        value = df.format(value).toBigDecimal()
        value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        return value.intValueExact().toString()
    }

    private fun formatUang(nilai : Double) : String{
        val localeID = Locale("in","ID")
        val rupiah = NumberFormat.getCurrencyInstance(localeID)
        return rupiah.format(nilai).toString()
    }



}
