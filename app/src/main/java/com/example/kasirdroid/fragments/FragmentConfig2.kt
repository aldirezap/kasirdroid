package com.example.kasirdroid.fragments


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.chaos.view.PinView
import com.example.kasirdroid.R
import com.example.kasirdroid.activity.ActivityHome
import com.example.kasirdroid.network.ApiServices
import com.example.kasirdroid.objek.DataClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.MessageDigest

/**
 * A simple [Fragment] subclass.
 */
class FragmentConfig2 : Fragment() {
    private var btnNext : Button? = null
    private var cache: SharedPreferences? = null
    private var editPin : PinView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cache = activity!!.getSharedPreferences(DataClient.SHARED_PREF_NAME, 0)

        editPin = activity!!.findViewById(R.id.editPin)
        btnNext = activity!!.findViewById(R.id.btnNext3)
        btnNext!!.setOnClickListener{
            if(editPin!!.text.isNullOrEmpty()){
                Toast.makeText(activity,"Kode aktivasi tidak boleh kosong.",Toast.LENGTH_SHORT).show()
                editPin!!.requestFocus()
            }else{
                if(editPin!!.text!!.length < 6){
                    Toast.makeText(activity,"Kode aktivasi harus 6 karakter.",Toast.LENGTH_SHORT).show()
                    editPin!!.requestFocus()
                }else{
                    serverCek()
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun serverCek(){
        val pin = encryptPIN(editPin!!.text.toString())
        val id = Settings.Secure.getString(context!!.contentResolver,Settings.Secure.ANDROID_ID)
        var nama: String
        var toko: String
        var alamat: String
        var telp: String

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiServices.URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val api = retrofit.create(ApiServices::class.java)
        val call = api.verifikasi("1",pin,id)
        call.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(activity,"Terjadi masalah pada koneksi internet.",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful){
                    if(response.body() != null){
                        val jsonresponse = response.body()
                        try {
                            val jsonObject = JSONObject(jsonresponse!!)
                            when {
                                jsonObject.optString("respon") == "first_registered" -> {
                                    nama = jsonObject.optString("owner")
                                    toko = jsonObject.optString("store")
                                    alamat = jsonObject.optString("address")
                                    telp = jsonObject.optString("contact")
                                    saveData(pin,id,nama,toko,alamat,telp)
                                }
                                jsonObject.optString("respon") == "registered" -> {
                                    nama = jsonObject.optString("owner")
                                    toko = jsonObject.optString("store")
                                    alamat = jsonObject.optString("address")
                                    telp = jsonObject.optString("contact")
                                    saveData(pin,id,nama,toko,alamat,telp)
                                }
                                jsonObject.optString("respon") == "max" -> {
                                    val builder = activity?.let { AlertDialog.Builder(it) }
                                    builder!!.setTitle("Pemberitahuan")
                                    builder.setMessage("Batas pengguna telah tercapai untuk produk ini. Silahkan hubungi penyedia untuk melakukan upgrade produk.")
                                    builder.setPositiveButton("Mengerti"){ _: DialogInterface, _: Int ->
                                        editPin!!.setText("")
                                        editPin!!.clearFocus()
                                    }

                                    val alertDialog : AlertDialog = builder.create()
                                    alertDialog.setCancelable(false)
                                    alertDialog.show()
                                }
                                else -> {
                                    val builder = activity?.let { AlertDialog.Builder(it) }
                                    builder!!.setTitle("Pemberitahuan")
                                    builder.setMessage("Produk dengan kode ini tidak ditemukan. Silahkan untuk membeli produk atau gunakan kode produk lain.")
                                    builder.setPositiveButton("Mengerti"){ _: DialogInterface, _: Int ->
                                        editPin!!.setText("")
                                        editPin!!.requestFocus()
                                    }

                                    val alertDialog : AlertDialog = builder.create()
                                    alertDialog.setCancelable(false)
                                    alertDialog.show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }else{
                    Toast.makeText(activity,"Koneksi server bermasalah.",Toast.LENGTH_SHORT).show()
                }
            }

        })

        /*if(encryptPIN(editPin!!.text.toString()) == "E5F70B9FAF1D9197FD1CAE3887EF99B9DBD3AA3474B68D6FFCBA381342D7574C3121857F39F79687F7729B6C51B0C1D4ED4F8C0B0F93750CBE68E68FAF9E1BF3"){
            saveData(pin,id,nama,toko,alamat,telp)
        }else{
            Toast.makeText(activity,"PIN yang Anda masukan salah.",Toast.LENGTH_SHORT).show()
            editPin!!.setText("")
            editPin!!.requestFocus()
        } */
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
        startActivity(Intent(activity, ActivityHome::class.java))
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
