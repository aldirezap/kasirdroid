package id.co.bigtek.kasir.activity

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import id.co.bigtek.kasir.R

class ActivityDashboard : AppCompatActivity() {

    private var btnBack : ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        inisialisasi()
    }

    private fun inisialisasi() {
        btnBack = findViewById(R.id.btnBack)

        loadFunction()
    }

    private fun loadFunction() {
        btnBack!!.setOnClickListener {
            back()
        }
    }

    private fun back(){
        finish()
    }

    override fun onBackPressed() {
        back()
    }
}
