package id.co.bigtek.kasir.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.activity.ActivityHome
import id.co.bigtek.kasir.objek.DataClient
import kotlinx.android.synthetic.main.fragment_config2.*
import java.security.MessageDigest

/**
 * A simple [Fragment] subclass.
 */
class FragmentConfig3 : Fragment() {
    private var btnNext : Button? = null
    private var cache: SharedPreferences? = null
    private var toko : EditText? = null
    private var alamat : EditText? = null
    private var kontak : EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config3, container, false)
    }

    @SuppressLint("HardwareIds")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cache = activity!!.getSharedPreferences(DataClient.SHARED_PREF_NAME, 0)

        toko = activity!!.findViewById(R.id.editNamaToko_Struk)
        alamat = activity!!.findViewById(R.id.editAlamatToko_Struk)
        kontak = activity!!.findViewById(R.id.editKontakToko_Struk)
        btnNext = activity!!.findViewById(R.id.btnNext4)
        btnNext!!.setOnClickListener{
            if(toko!!.text.isNullOrEmpty()){
                Toast.makeText(activity,"Nama toko tidak boleh kosong.",Toast.LENGTH_SHORT).show()
                editPin!!.requestFocus()
            }else{
                val id = Settings.Secure.getString(context!!.contentResolver, Settings.Secure.ANDROID_ID)
                saveData(encryptPIN("000000"),id,"BIGTEK",toko!!.text.toString(),alamat!!.text.toString(),kontak!!.text.toString())
            }
        }
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
        startActivity(Intent(activity,ActivityHome::class.java))
    }

    private fun encryptPIN(pin : String): String {
        val hexChars = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance("SHA-512")
            .digest(pin.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }

        return result.toString()
    }


}
