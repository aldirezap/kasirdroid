package com.example.kasirdroid.adapter.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.kasirdroid.R
import com.example.kasirdroid.activity.ActivityHome
import com.example.kasirdroid.model.list.ModelListProduk
import com.example.kasirdroid.objek.DBKasir
import java.text.NumberFormat
import java.util.*


class AdapterListProduk(private val context: Context, private val produk: ArrayList<ModelListProduk>) :
    RecyclerView.Adapter<AdapterListProduk.ViewHolder>() {

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_list_produk, null)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return produk.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val home = context as ActivityHome
        //mengisi data konten
        holder.namaProduk.text = produk[position].namaProduk
        holder.kodeProduk.text = "ID:${produk[position].kodeProduk} (${produk[position].stok} item)"
        holder.hargaJualProduk.text = formatUang(produk[position].hargaJual!!.toDouble())
        holder.number.text = produk[position].number.toString()

        //klik selft
        /*holder.itemView.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Apakah Anda ingin mengahapus produk ini? \nMenghapus data produk akan mempengaruhi data lain seperti data transaksi.")
            builder.setPositiveButton("Ya"){ _: DialogInterface, _: Int ->
                val deleteQ = "DELETE FROM produk WHERE produk_kode = '${produk[position].kodeProduk}'"
                context.dbHelper.customQuery(deleteQ)

                produk.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position,produk.size)

                Toast.makeText(context,"Produk telah dihapus.",Toast.LENGTH_SHORT).show()
            }

            builder.setNeutralButton("Tidak"){ _: DialogInterface, _: Int ->

            }

            val alertDialog : AlertDialog = builder.create()
            alertDialog.show()

            true
        } */

        holder.itemView.setOnClickListener {
            val mDialogView = LayoutInflater.from(context).inflate(R.layout.content_alert_edit_produk, null)
            val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)

            mDialogView.findViewById<EditText>(R.id.editNamaProduk).setText(produk[position].namaProduk)
            mDialogView.findViewById<EditText>(R.id.editHargaPokok).setText(produk[position].hargaPokok.toString())
            mDialogView.findViewById<EditText>(R.id.editHargaJual).setText(produk[position].hargaJual.toString())
            mDialogView.findViewById<EditText>(R.id.editStokProduk).setText(produk[position].stok.toString())
            mDialogView.findViewById<EditText>(R.id.editDiskonProduk).setText(produk[position].diskon.toString())

            val mAlertDialog = mBuilder.create()
            mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mAlertDialog.show()
            mAlertDialog.setCancelable(true)

            //val kategori = mDialogView.findViewById<EditText>(R.id.txtNamaKategori).text
            //mDialogView.findViewById<Button>(R.id.btnAlertTambahKategori).setOnClickListener {}

            mDialogView.findViewById<Button>(R.id.btnCloseEditProduk).setOnClickListener {
                mAlertDialog.dismiss()
            }

            mDialogView.findViewById<Button>(R.id.btnDeleteProduk).setOnClickListener {
                val queryDel = "DELETE FROM produk WHERE produk_kode = '${produk[position].kodeProduk}'"
                context.dbHelper.customQuery(queryDel)
                produk.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position,produk.size)
                Toast.makeText(context,"Produk telah dihapus.",Toast.LENGTH_SHORT).show()
                mAlertDialog.dismiss()
            }

            //button edit
            mDialogView.findViewById<ImageButton>(R.id.btnEditNamaProduk).setOnClickListener {
                mDialogView.findViewById<EditText>(R.id.editNamaProduk).isEnabled = true
                mDialogView.findViewById<EditText>(R.id.editNamaProduk).requestFocus()

                mDialogView.findViewById<ImageButton>(R.id.btnEditNamaProduk).visibility = View.GONE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveNamaProduk).visibility = View.VISIBLE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnEditHargaPokokProduk).setOnClickListener {
                mDialogView.findViewById<EditText>(R.id.editHargaPokok).isEnabled = true
                mDialogView.findViewById<EditText>(R.id.editHargaPokok).requestFocus()

                mDialogView.findViewById<ImageButton>(R.id.btnEditHargaPokokProduk).visibility = View.GONE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveHargaPokokProduk).visibility = View.VISIBLE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnEditHargaJualProduk).setOnClickListener {
                mDialogView.findViewById<EditText>(R.id.editHargaJual).isEnabled = true
                mDialogView.findViewById<EditText>(R.id.editHargaJual).requestFocus()

                mDialogView.findViewById<ImageButton>(R.id.btnEditHargaJualProduk).visibility = View.GONE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveHargaJualProduk).visibility = View.VISIBLE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnEditStokProduk).setOnClickListener {
                mDialogView.findViewById<EditText>(R.id.editStokProduk).isEnabled = true
                mDialogView.findViewById<EditText>(R.id.editStokProduk).requestFocus()

                mDialogView.findViewById<ImageButton>(R.id.btnEditStokProduk).visibility = View.GONE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveStokProduk).visibility = View.VISIBLE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnEditDiskonProduk).setOnClickListener {
                mDialogView.findViewById<EditText>(R.id.editDiskonProduk).isEnabled = true
                mDialogView.findViewById<EditText>(R.id.editDiskonProduk).requestFocus()

                mDialogView.findViewById<ImageButton>(R.id.btnEditDiskonProduk).visibility = View.GONE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveDiskonProduk).visibility = View.VISIBLE
            }

            //button save
            mDialogView.findViewById<ImageButton>(R.id.btnSaveNamaProduk).setOnClickListener {
                saveData(holder, position, mDialogView)
                mDialogView.findViewById<EditText>(R.id.editNamaProduk).isEnabled = false

                mDialogView.findViewById<ImageButton>(R.id.btnEditNamaProduk).visibility = View.VISIBLE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveNamaProduk).visibility = View.GONE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnSaveHargaPokokProduk).setOnClickListener {
                saveData(holder, position, mDialogView)
                mDialogView.findViewById<EditText>(R.id.editHargaPokok).isEnabled = false

                mDialogView.findViewById<ImageButton>(R.id.btnEditHargaPokokProduk).visibility = View.VISIBLE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveHargaPokokProduk).visibility = View.GONE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnSaveHargaJualProduk).setOnClickListener {
                saveData(holder, position, mDialogView)
                mDialogView.findViewById<EditText>(R.id.editHargaJual).isEnabled = false

                mDialogView.findViewById<ImageButton>(R.id.btnEditHargaJualProduk).visibility = View.VISIBLE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveHargaJualProduk).visibility = View.GONE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnSaveStokProduk).setOnClickListener {
                saveData(holder, position, mDialogView)
                mDialogView.findViewById<EditText>(R.id.editStokProduk).isEnabled = false

                mDialogView.findViewById<ImageButton>(R.id.btnEditStokProduk).visibility = View.VISIBLE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveStokProduk).visibility = View.GONE
            }

            mDialogView.findViewById<ImageButton>(R.id.btnSaveDiskonProduk).setOnClickListener {
                saveData(holder, position, mDialogView)
                mDialogView.findViewById<EditText>(R.id.editDiskonProduk).isEnabled = false

                mDialogView.findViewById<ImageButton>(R.id.btnEditDiskonProduk).visibility = View.VISIBLE
                mDialogView.findViewById<ImageButton>(R.id.btnSaveDiskonProduk).visibility = View.GONE
            }

        }
    }

    private fun formatUang(nilai : Double) : String{
        val localeID = Locale("in","ID")
        val rupiah = NumberFormat.getCurrencyInstance(localeID)
        return rupiah.format(nilai).toString()
    }

    @SuppressLint("SetTextI18n")
    private fun saveData(
        holder: ViewHolder,
        position: Int,
        mDialogView: View
    ) {
        val home = context as ActivityHome
        val query = "UPDATE "+ DBKasir.TabelProduk.NAMA_TABEL+" set "+
                DBKasir.TabelProduk.PRODUK_NAMA+" = '"+ mDialogView.findViewById<EditText>(R.id.editNamaProduk).text+"',"+
                DBKasir.TabelProduk.PRODUK_HARGA+" = "+ mDialogView.findViewById<EditText>(R.id.editHargaPokok).text+","+
                DBKasir.TabelProduk.PRODUK_HARGA_JUAL+" = "+ mDialogView.findViewById<EditText>(R.id.editHargaJual).text+","+
                DBKasir.TabelProduk.PRODUK_STOK+" = "+ mDialogView.findViewById<EditText>(R.id.editStokProduk).text+","+
                DBKasir.TabelProduk.PRODUK_DISKON+" = "+ mDialogView.findViewById<EditText>(R.id.editDiskonProduk).text+
                " WHERE "+DBKasir.TabelProduk.PRODUK_KODE+" = '"+produk[position].kodeProduk+"'"
        holder.namaProduk.text =  mDialogView.findViewById<EditText>(R.id.editNamaProduk).text
        holder.hargaJualProduk.text = "Rp "+ mDialogView.findViewById<EditText>(R.id.editHargaJual).text
        home.dbHelper.customQuery(query)
        Toast.makeText(context,"Perubahan disimpan.",Toast.LENGTH_SHORT).show()
    }

    inner class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        var number: TextView = itemView.findViewById(R.id.txtNumber)
        var namaProduk: TextView = itemView.findViewById(R.id.txtNamaProduk)
        var kodeProduk: TextView = itemView.findViewById(R.id.txtKodeProduk)
        var hargaJualProduk: TextView = itemView.findViewById(R.id.txtHargaJualProduk)
    }
}