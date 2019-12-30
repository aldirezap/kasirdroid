package id.co.bigtek.kasir.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.helper.DBHelper
import id.co.bigtek.kasir.model.database.ModelKategori
import id.co.bigtek.kasir.model.database.ModelProduk
import id.co.bigtek.kasir.network.ApiServices
import id.co.bigtek.kasir.objek.DBKasir
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.InputStream


class ActivityAddProduk : AppCompatActivity() {
    private var produkKode : String? = null
    private var produkNama : String? = null
    private var produkHargaPokok : Int? = null
    private var produkHargaJual : Int? = null
    private var produkStok : Int? = null
    private var produkDiskon : Int? = null

    private var txtProdukKode : EditText? = null
    private var txtProdukNama : EditText? = null
    private var txtProdukHargaPokok : EditText? = null
    private var txtProdukHargaJual : EditText? = null
    private var txtProdukStok : EditText? = null
    private var txtProdukDiskon : EditText? = null
    private var checkBox : CheckBox? = null
    private var txtImport : TextView? = null

    private var spinnerKategori : Spinner? = null
    private var btnAddKategori : ImageButton? = null

    private var importPath : String = ""
    private var contentUri : Uri? = null

    private lateinit var dbHelper : DBHelper
    private lateinit var mInterstitialAd: InterstitialAd

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_produk)
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

        inisialisasi()
        showAds()
    }

    private fun inisialisasi() {
        checkBox = findViewById(R.id.checkBox)
        txtProdukKode = findViewById(R.id.txtKodeProduk)
        txtProdukNama = findViewById(R.id.txtNamaProduk)
        txtProdukHargaPokok = findViewById(R.id.txtHargaPokok)
        txtProdukHargaJual = findViewById(R.id.txtHargaJual)
        txtProdukStok = findViewById(R.id.txtStokProduk)
        txtProdukDiskon = findViewById(R.id.txtDiskonProduk)
        spinnerKategori = findViewById(R.id.spinner_kategori)
        btnAddKategori = findViewById(R.id.btnAddKategori)

        loadFunction()
    }

    private fun loadFunction(){
        btnAddKategori!!.setOnClickListener {
            addKategori()
        }

        loadKategori()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack!!.setOnClickListener {
            back()
        }

        val btnTambah = findViewById<Button>(R.id.btnTambahProduk)
        btnTambah!!.setOnClickListener {
            tambahProduk()
        }



    }

    private fun loadKategori(){
        val spinnerArray = ArrayList<String>()
        spinnerArray.clear()

        val sql = "SELECT * FROM kategori"
        val cursor : Cursor = dbHelper.customQuery(sql)!!
        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            spinnerArray.add(cursor.getString(cursor.getColumnIndex(DBKasir.TabelKategori.KATEGORI_PRODUK)))
            cursor.moveToNext()
        }
        val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, spinnerArray)
        spinnerKategori!!.adapter = adapter
    }

    private fun addKategori(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.content_alert_kategori_add, null)
        val mBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)

        val kategori = mDialogView.findViewById<EditText>(R.id.txtNamaKategori).text
        mDialogView.findViewById<Button>(R.id.btnAlertTambahKategori).setOnClickListener {
            if(kategori.isEmpty()){
                Toast.makeText(this,"Harap isi nama kategori!",Toast.LENGTH_SHORT).show()
            }else{
                val cekKategori = "SELECT COUNT(*) FROM kategori WHERE kategori_produk = '$kategori'"
                if(dbHelper.customQuery(cekKategori)!!.getInt(0) == 0){
                    val kode = dbHelper.customQuery("SELECT MAX(kategori_id) FROM kategori")!!.getInt(0)+1
                    dbHelper.insertKategori(ModelKategori(kode = kode,nama = kategori.toString()))
                    Toast.makeText(this,"Kategori baru ditambahkan.",Toast.LENGTH_SHORT).show()
                    loadKategori()
                    mAlertDialog.dismiss()
                }else{
                    Toast.makeText(this,"Kategori serupa telah ada.",Toast.LENGTH_SHORT).show()
                }
            }
        }

        mDialogView.findViewById<Button>(R.id.btnAlertBatalTambahKategori).setOnClickListener {
            mAlertDialog.dismiss()
        }
    }


    private fun tambahProduk() {
        if(txtProdukKode!!.text.isEmpty() ||
                txtProdukNama!!.text.isEmpty() ||
                txtProdukHargaPokok!!.text.isEmpty() ||
                txtProdukHargaJual!!.text.isEmpty() ||
                spinnerKategori!!.selectedItem.toString().isEmpty()){
            Toast.makeText(this,"Form masih ada yang kosong.",Toast.LENGTH_SHORT).show()
        }else{
            produkKode = txtProdukKode!!.text.toString()
            produkNama = txtProdukNama!!.text.toString()
            produkHargaPokok = Integer.valueOf(txtProdukHargaPokok!!.text.toString())
            produkHargaJual = Integer.valueOf(txtProdukHargaJual!!.text.toString())

            produkStok = if(txtProdukStok!!.text.isEmpty()){
                0
            }else{
                Integer.valueOf(txtProdukStok!!.text.toString())
            }

            produkDiskon = if(txtProdukDiskon!!.text.isEmpty()){
                0
            }else{
                Integer.valueOf(txtProdukDiskon!!.text.toString())
            }

            val query = "Select COUNT(*) FROM "+DBKasir.TabelProduk.NAMA_TABEL+" WHERE "+DBKasir.TabelProduk.PRODUK_KODE+" = '"+txtProdukKode!!.text.toString()+"'"
            if(Integer.valueOf(dbHelper.customQuery(query)!!.getString(0)) > 0){
                Toast.makeText(this,"Produk dengan kode serupa telah ada.",Toast.LENGTH_SHORT).show()
            }else{
                var id = spinnerKategori!!.selectedItem.toString()
                val getIdKategori = "SELECT kategori_id FROM kategori WHERE kategori_produk = '$id'"
                id = dbHelper.customQuery(getIdKategori)!!.getString(0)
                if(dbHelper.insertProduk(
                        ModelProduk(
                            kode = produkKode!!,
                            nama = produkNama!!,
                            harga = produkHargaPokok!!.toDouble(),
                            harga_jual = produkHargaJual!!.toDouble(),
                            stok = produkStok!!,
                            diskon = produkDiskon!!,
                            kategori_id = id.toInt()
                        )
                    )){
                    if(checkBox!!.isChecked){
                        txtProdukKode!!.setText("")
                        txtProdukNama!!.setText("")
                        txtProdukHargaPokok!!.setText("")
                        txtProdukHargaJual!!.setText("")
                        txtProdukStok!!.setText("")
                        txtProdukDiskon!!.setText("")
                        txtProdukKode!!.requestFocus()
                        Toast.makeText(this,"Data berhasil ditambahkan",Toast.LENGTH_SHORT).show()
                        showAds()
                    } else{
                        Toast.makeText(this,"Data berhasil ditambahkan",Toast.LENGTH_SHORT).show()
                        showAds()
                    }
                }else{
                    txtProdukKode!!.requestFocus()
                    Toast.makeText(this,"Data gagal ditambahkan, silahkan periksa Kode Produk.",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 111 && resultCode == Activity.RESULT_OK){
            contentUri = data!!.data
            txtImport!!.text = data.data!!.path!!
            importPath = data.data!!.path!!
        }
    }

    override fun onBackPressed() {
        back()
    }

    private fun back(){
        //startActivity(Intent(this,ActivityHome::class.java))
        finish()
    }

    private fun showAds(){
        mInterstitialAd.show()
    }
}
