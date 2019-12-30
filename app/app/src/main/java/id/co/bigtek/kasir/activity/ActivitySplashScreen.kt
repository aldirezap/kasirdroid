package id.co.bigtek.kasir.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.objek.DataClient

class ActivitySplashScreen : AppCompatActivity() {
    private var cache: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        cache = getSharedPreferences(DataClient.SHARED_PREF_NAME, 0)

        loadTime()
    }

    private fun loadTime() {
        val timer = object : CountDownTimer(3000,1000) {
            override fun onFinish() {
                val used = cache!!.getBoolean(DataClient.USED, false)
                if(!used){
                    startActivity(Intent(this@ActivitySplashScreen,ActivityConfigurasi::class.java))
                }else{
                    startActivity(Intent(this@ActivitySplashScreen,ActivityHome::class.java))
                }
            }

            override fun onTick(p0: Long) {

            }
        }
        timer.start()
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveData(
        pin: String,
        id: String,
        nama: String,
        toko: String,
        alamat: String,
        telp: String
    ) {
        val editor = cache!!.edit()
        editor.putBoolean(DataClient.USED,true)
        editor.putString(DataClient.PIN,pin)
        editor.putString(DataClient.CLIENT,id)
        editor.putString(DataClient.NAMA,nama)
        editor.putString(DataClient.TOKO,toko)
        editor.putString(DataClient.ALAMAT,alamat)
        editor.putString(DataClient.TELP,telp)
        editor.apply()
        changeLayout()
    }

    private fun changeLayout(){
        startActivity(Intent(this@ActivitySplashScreen,ActivityHome::class.java))
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
