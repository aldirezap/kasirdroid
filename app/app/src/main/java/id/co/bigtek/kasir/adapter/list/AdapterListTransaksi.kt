package id.co.bigtek.kasir.adapter.list

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.activity.ActivityTransaksi
import id.co.bigtek.kasir.model.list.ModelListTransaksi
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


class AdapterListTransaksi(private val context: Context, private val produk: ArrayList<ModelListTransaksi>) :
    RecyclerView.Adapter<AdapterListTransaksi.ViewHolder>() {

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.content_list_transaksi, null)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return produk.size
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (context as ActivityTransaksi)
        //mengisi data konten
        holder.namaProduk.text = context.dbHelper.customQuery("SELECT produk_nama FROM produk WHERE produk_kode = '${produk[position].kodeProduk}'")!!.getString(0)
        holder.jumlahItem.text = produk[position].qty.toString()

        val getStokProduk = "SELECT produk_stok FROM produk WHERE produk_kode = '${produk[position].kodeProduk}'"
        val stokProduk = context.dbHelper.customQuery(getStokProduk)!!.getInt(0)

        val getHargaProduk = "SELECT produk_harga_jual FROM produk WHERE produk_kode = '${produk[position].kodeProduk}'"
        val harga = context.dbHelper.customQuery(getHargaProduk)!!.getDouble(0)

        val getDiskonProduk = "SELECT produk_diskon FROM produk WHERE produk_kode = '${produk[position].kodeProduk}'"
        val diskon = context.dbHelper.customQuery(getDiskonProduk)!!.getInt(0)

        val qty = produk[position].qty!!.toDouble()
        var hargaAkhir = harga*qty
        holder.hargaProduk.text = formatUang(harga)

        val jumlahPotongan = (diskon/100)*hargaAkhir
        hargaAkhir -= jumlahPotongan

        holder.totalHarga.text = "Rp${formatUang(hargaAkhir)}"

        var stok = produk[position].qty!!.toInt()
        holder.btnHapus.setOnClickListener {
            val query = "DELETE FROM detail_transaksi WHERE detail_kode = "+produk[position].kodeDetailTransaksi

            val builder = AlertDialog.Builder(context)
            builder.setMessage("Apakah Anda yakin ingin menghapus item ini?")
            builder.setPositiveButton("Ya"){ _: DialogInterface, _: Int ->
                context.dbHelper.customQuery(query)
                context.listTransaksi()
            }

            builder.setNegativeButton("Tidak"){ _: DialogInterface, _: Int ->

            }

            val alertDialog : AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        holder.btnTambah.setOnClickListener {
            if(stok == stokProduk){
                Toast.makeText(context,"Stok habis.",Toast.LENGTH_SHORT).show()
            }else{
                stok +=1
                holder.jumlahItem.text = stok.toString()

                val idDetail = produk[position].kodeDetailTransaksi
                val query = "UPDATE detail_transaksi SET detail_qty = $stok WHERE detail_kode = $idDetail"
                context.dbHelper.customQuery(query)
                context.listTransaksi()
            }
        }

        holder.btnKurangi.setOnClickListener {
            if(stok > 1){
                stok -=1
                holder.jumlahItem.text = stok.toString()

                val idDetail = produk[position].kodeDetailTransaksi
                val query = "UPDATE detail_transaksi SET detail_qty = $stok WHERE detail_kode = $idDetail"
                context.dbHelper.customQuery(query)
                context.listTransaksi()
            }
        }

    }

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


    inner class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        //var hargaProduk: TextView = itemView.findViewById(R.id.txtResultHargaProduk)
        var namaProduk: TextView = itemView.findViewById(R.id.txtResultNamaProduk)
        var jumlahItem: TextView = itemView.findViewById(R.id.txtBanyakItem)
        var totalHarga: TextView = itemView.findViewById(R.id.txtTotalHargaBarang)
        var btnTambah: Button = itemView.findViewById(R.id.btnTambahJumlahBeli)
        var btnKurangi: Button = itemView.findViewById(R.id.btnKurangiJumlahBeli)
        var btnHapus: ImageButton = itemView.findViewById(R.id.btnDeleteProdukFromListTransaksi)
        var hargaProduk : TextView = itemView.findViewById(R.id.txtResultHargaProduk)
    }
}