package com.example.kasirdroid.adapter.list

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.kasirdroid.R
import com.example.kasirdroid.activity.ActivityHome
import com.example.kasirdroid.model.list.ModelListRiwayatTransaksi
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class AdapterListRiwayatTransaksi(private val context: Context, private val transaksi: ArrayList<ModelListRiwayatTransaksi>) :
    RecyclerView.Adapter<AdapterListRiwayatTransaksi.ViewHolder>() {

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_list_riwayat_transaksi, null)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return transaksi.size
    }

    @SuppressLint("SetTextI18n", "InflateParams", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (context as ActivityHome)
        holder.nomorTRansaksi.text = "Transaksi #"+transaksi[position].kodeTransaksi
        holder.totalHarga.text = formatUang(transaksi[position].subTotal!!.toDouble())

        val format = SimpleDateFormat("dd-MM-yyyy")
        val formatTanggal = SimpleDateFormat("dd")
        val formatBulan = SimpleDateFormat("MMM")

        val dateTgl : Date = format.parse(transaksi[position].tanggal)
        val dateBln : Date = format.parse(transaksi[position].tanggal)

        val tgl = formatTanggal.format(dateTgl)
        val bln = formatBulan.format(dateBln)

        holder.tanggal.text = tgl
        holder.bulan.text = bln

        val cekJumlahProduk = "SELECT COUNT(*) FROM detail_transaksi WHERE transaksi_kode = "+transaksi[position].kodeTransaksi
        val jumlahProduk = context.dbHelper.customQuery(cekJumlahProduk)!!.getInt(0)

        val cekJumlahQty = "SELECT SUM(detail_qty) FROM detail_transaksi WHERE transaksi_kode = "+transaksi[position].kodeTransaksi
        val jumlahQty = context.dbHelper.customQuery(cekJumlahQty)!!.getInt(0)

        holder.detailTransaksi.text = "$jumlahProduk Produk ($jumlahQty Item)"

        holder.itemView.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Anda dapat menghapus atau melakukan print ulang(Struk) data transaksi ini.")
            builder.setPositiveButton(""){ _: DialogInterface, _: Int ->
            }

            builder.setNegativeButton("Hapus"){ _: DialogInterface, _: Int ->
                val hapusTransaksi = "DELETE FROM transaksi WHERE transaksi_kode = "+transaksi[position].kodeTransaksi
                val hapusDetailtransaksi = "DELETE FROM detail_transaksi WHERE transaksi_kode = "+transaksi[position].kodeTransaksi
                context.dbHelper.customQuery(hapusTransaksi)
                context.dbHelper.customQuery(hapusDetailtransaksi)

                transaksi.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position,transaksi.size)

                Toast.makeText(context,"Transaksi telah dihapus.",Toast.LENGTH_SHORT).show()
            }

            builder.setNeutralButton("Batalkan"){ _: DialogInterface, _: Int ->

            }

            val alertDialog : AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    @SuppressLint("SimpleDateFormat")


    private fun formatUang(nilai : Double) : String{
        val localeID = Locale("in","ID")
        val rupiah = NumberFormat.getCurrencyInstance(localeID)
        return rupiah.format(nilai).toString()
    }


    inner class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        var detailTransaksi: TextView = itemView.findViewById(R.id.txtDetailDataTransaksi)
        var totalHarga: TextView = itemView.findViewById(R.id.txtJumlahBelanjaan)
        var tanggal : TextView = itemView.findViewById(R.id.txtRiwayatTanggalTransaksi)
        var bulan : TextView = itemView.findViewById(R.id.txtRiwayatBulanTransaksi)
        var nomorTRansaksi : TextView = itemView.findViewById(R.id.txtNomorTransaksi)
    }
}