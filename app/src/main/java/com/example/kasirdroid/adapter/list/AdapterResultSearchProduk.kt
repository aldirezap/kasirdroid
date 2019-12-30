package com.example.kasirdroid.adapter.list

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.kasirdroid.R
import com.example.kasirdroid.activity.ActivityTransaksi
import com.example.kasirdroid.model.database.ModelDetailTransaksi
import com.example.kasirdroid.model.list.ModelResultSearchProduk
import com.example.kasirdroid.objek.DBKasir




class AdapterResultSearchProduk(private val context: Context, private val produk: ArrayList<ModelResultSearchProduk>) :
    RecyclerView.Adapter<AdapterResultSearchProduk.ViewHolder>() {

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_list_search_result, null)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return produk.size
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //mengisi data konten
        holder.namaProduk.text = produk[position].namaProduk
        holder.hargaProduk.text = produk[position].kodeProduk

        //klik selft
        holder.itemView.setOnClickListener {
            if(produk[position].stok!!.toInt() > 0){
                var stok = 1
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.content_alert_jumlah_beli, null)
                val mBuilder = AlertDialog.Builder(context)
                    .setView(mDialogView)
                mDialogView.findViewById<TextView>(R.id.txtJumlahPembelian).text = stok.toString()
                mDialogView.findViewById<ImageButton>(R.id.btnHapusDariTransaksi).visibility = View.GONE
                val mAlertDialog = mBuilder.show()

                mDialogView.findViewById<ImageButton>(R.id.btnTambahJumlahBeli).setOnClickListener {
                    if(stok == produk[position].stok){
                        Toast.makeText(context,"Stok habis.",Toast.LENGTH_SHORT).show()
                    }else{
                        stok +=1
                        mDialogView.findViewById<TextView>(R.id.txtJumlahPembelian).text = stok.toString()
                    }
                }

                mDialogView.findViewById<ImageButton>(R.id.btnKurangiJumlahBeli).setOnClickListener {
                    if(stok > 1){
                        stok -=1
                        mDialogView.findViewById<TextView>(R.id.txtJumlahPembelian).text = stok.toString()
                    }
                }

                mDialogView.findViewById<Button>(R.id.btnConfirmJumlahBeli).setOnClickListener {
                    val idTransaksi = (context as ActivityTransaksi).idTransaksi
                    val query1 = "Select COUNT(*) FROM "+ DBKasir.TabelDetailTransaksi.NAMA_TABEL+" WHERE "+DBKasir.TabelDetailTransaksi.TRANSAKSI_KODE+" = "+idTransaksi+" AND "+DBKasir.TabelDetailTransaksi.PRODUK_KODE+" = '"+produk[position].kodeProduk+"'"
                    if(context.dbHelper.customQuery(query1)!!.getInt(0) == 1){
                        var query = "Select * FROM "+DBKasir.TabelDetailTransaksi.NAMA_TABEL+" WHERE "+DBKasir.TabelDetailTransaksi.TRANSAKSI_KODE+" = "+idTransaksi+" AND "+DBKasir.TabelDetailTransaksi.PRODUK_KODE+" = '"+produk[position].kodeProduk+"'"
                        val cursor : Cursor = context.dbHelper.customQuery(query)!!
                        cursor.moveToFirst()
                        val idDetail = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_KODE))
                        val qty = cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_QTY))+stok

                        query = "UPDATE "+DBKasir.TabelDetailTransaksi.NAMA_TABEL+" SET "+DBKasir.TabelDetailTransaksi.DETAIL_QTY+" = "+qty+" WHERE "+DBKasir.TabelDetailTransaksi.DETAIL_KODE+" = "+idDetail
                        context.dbHelper.customQuery(query)

                        Toast.makeText(context,"Jumlah item ditambah.",Toast.LENGTH_SHORT).show()
                        context.listTransaksi()
                    }else{
                        val query2 = "SELECT MAX(detail_kode) FROM detail_transaksi"
                        val id: Int?
                        id = if(context.dbHelper.customQuery(query2)!!.getString(0).isNullOrEmpty()){
                            1
                        }else{
                            context.dbHelper.customQuery(query2)!!.getInt(0)+1
                        }

                        context.dbHelper.insertDetailTransaksi(
                            ModelDetailTransaksi(
                                kodeDetailTransaksi = id,
                                kodeTransaksi = idTransaksi,
                                kodeProduk = produk[position].kodeProduk.toString(),
                                qty = stok
                            )
                        )
                        Toast.makeText(context,"Item ditambahkan.",Toast.LENGTH_SHORT).show()
                        context.listTransaksi()
                    }
                    mAlertDialog.dismiss()
                }
            }else{
                Toast.makeText(context,"Stok produk habis.",Toast.LENGTH_SHORT).show()
            }
        }
    }


    inner class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        var hargaProduk: TextView = itemView.findViewById(R.id.txtResultHargaProduk)
        var namaProduk: TextView = itemView.findViewById(R.id.txtResultNamaProduk)
    }
}