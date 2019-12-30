package id.co.bigtek.kasir.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.viewpager.widget.ViewPager
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.adapter.AdapterConfigurasi
import id.co.bigtek.kasir.helper.DBHelper

class ActivityConfigurasi : AppCompatActivity() {
    var pagerConfigurasi : ViewPager? = null
    private lateinit var dbHelper : DBHelper
    private var adapterConfigurasi : AdapterConfigurasi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configurasi)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        dbHelper = DBHelper(this)

        pagerConfigurasi = findViewById(R.id.pagerConfigurasi)
        adapterConfigurasi = AdapterConfigurasi(supportFragmentManager)
        pagerConfigurasi!!.adapter = adapterConfigurasi
        pagerConfigurasi!!.offscreenPageLimit = 2

        //agreePermission()
    }

    private fun agreePermission() {
        val requestCode = 232
        val permissions =
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE)
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

    fun changeLayout(a : Int){
        if(a > 1){
            startActivity(Intent(this,ActivityHome::class.java))
        }else{
            pagerConfigurasi!!.currentItem = a
            adapterConfigurasi!!.notifyDataSetChanged()
        }
    }

    fun saveData(nama : String, kontak : String, alamat : String, pin : String){
        //dbHelper.insertAkun(ModelAkun(nama = nama,kontak = kontak,alamat = alamat,pin = pin,id = 1))
        changeLayout(2)
    }

    override fun onBackPressed() {
        if(pagerConfigurasi!!.currentItem == 0){
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }else{
            changeLayout(pagerConfigurasi!!.currentItem-1)
        }
    }
}
