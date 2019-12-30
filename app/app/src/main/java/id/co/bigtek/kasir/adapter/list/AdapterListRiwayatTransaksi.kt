package id.co.bigtek.kasir.adapter.list

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
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfWriter
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import id.co.bigtek.kasir.R
import id.co.bigtek.kasir.activity.ActivityHome
import id.co.bigtek.kasir.model.list.ModelListRiwayatTransaksi
import id.co.bigtek.kasir.objek.DBKasir
import id.co.bigtek.kasir.objek.DataClient
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
            builder.setPositiveButton("Print Ulang"){ _: DialogInterface, _: Int ->
                printStruk(position,context)
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
    private fun printStruk(position: Int, context: ActivityHome) {
        val cache: SharedPreferences? = context.getSharedPreferences(DataClient.SHARED_PREF_NAME, 0)
        val formatTanggal = SimpleDateFormat("EEEE, dd MMMM yyyy")
        val tanggal = formatTanggal.format(Date())
        val toko = cache!!.getString(DataClient.TOKO,"toko")
        val alamat = cache.getString(DataClient.ALAMAT,"alamat")
        val telp = cache.getString(DataClient.TELP,"telp")
        //val kasir = cache.getString(DataClient.NAMA,"nama")

        val noTransaksi = transaksi[position].kodeTransaksi
        val batas = "------------------------------------------"
        var jmlItem = 0
        var jmlProduk = 0

        val printables = ArrayList<Printable>()
        printables.clear()

        //Tulis Nama Toko
        var printable = TextPrintable.Builder()
            .setText(toko!!)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        //Tulis Alamat Toko
        printable = TextPrintable.Builder()
            .setText(alamat!!)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        //Tulis Telpon Toko
        printable = TextPrintable.Builder()
            .setText(telp!!)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(2)
            .build()
        printables.add(printable)
        //Tulis No Transaksi
        printable = TextPrintable.Builder()
            .setText("No.Transaksi ")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .build()
        printables.add(printable)
        printable = TextPrintable.Builder()
            .setText(noTransaksi.toString())
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        printable = TextPrintable.Builder()
            .setText(tanggal)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)

        //Tulis Batas Atas
        printable = TextPrintable.Builder()
            .setText(batas)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)

        //DAFTAR BARANG
        val getData = "SELECT * FROM detail_transaksi WHERE transaksi_kode = $noTransaksi"
        val cursor : Cursor = context.dbHelper.customQuery(getData)!!
        cursor.moveToFirst()
        for(i in 0 until cursor.count){
            jmlProduk += 1
            jmlItem += cursor.getInt(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_QTY))
            val qtyBeli = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.DETAIL_QTY))
            var diskonProduk: Int
            val writeDiskon: String

            val kodeProduk = cursor.getString(cursor.getColumnIndex(DBKasir.TabelDetailTransaksi.PRODUK_KODE))
            val getDiskon = "SELECT produk_diskon FROM produk WHERE produk_kode = '$kodeProduk'"
            diskonProduk = context.dbHelper.customQuery(getDiskon)!!.getInt(0)
            writeDiskon = if(diskonProduk > 0){
                " (-$diskonProduk%)"
            }else{
                ""
            }

            val getNamaProduk = "SELECT produk_nama FROM produk WHERE produk_kode = '$kodeProduk'"
            val namaProduk = context.dbHelper.customQuery(getNamaProduk)!!.getString(0)

            val getHargaJual = "SELECT produk_harga_jual FROM produk WHERE produk_kode = '$kodeProduk'"
            val hargaJual = context.dbHelper.customQuery(getHargaJual)!!.getDouble(0)
            val totalBeliProduk = hargaJual.toBigDecimal()*qtyBeli.toBigDecimal()

            //Tulis Nama Produk
            printable = TextPrintable.Builder()
                .setText(namaProduk)
                .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                .build()
            printables.add(printable)
            //Tulis Total Harga
            printable = TextPrintable.Builder()
                .setText(formatUang(totalBeliProduk.toDouble()))
                .setAlignment(DefaultPrinter.ALIGNMENT_RIGHT)
                .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                .setNewLinesAfter(1)
                .build()
            printables.add(printable)
            //Tulis QTY Beli x Harga Barang
            printable = TextPrintable.Builder()
                .setText("$qtyBeli x ${formatUang(hargaJual)}")
                .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
                .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                .build()
            printables.add(printable)
            //Tulis Diskon
            printable = TextPrintable.Builder()
                .setText(writeDiskon)
                .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
                .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                .setNewLinesAfter(1)
                .build()
            printables.add(printable)
            cursor.moveToNext()
        }

        //Tulis Batas Bawah
        printable = TextPrintable.Builder()
            .setText(batas)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        //Tulis Jumlah Item
        /*printable = TextPrintable.Builder()
            .setText("Item : $jmlProduk")
            .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        //Tulis Jumlah QTY
        printable = TextPrintable.Builder()
            .setText("Qty : $jmlItem")
            .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable) */
        //Tulis Jumlah Total
        /*printable = TextPrintable.Builder()
            .setText("Total")
            .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .build()
        printables.add(printable) */
        printable = TextPrintable.Builder()
            .setText(formatUang(transaksi[position].total!!.toDouble()))
            .setAlignment(DefaultPrinter.ALIGNMENT_RIGHT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)

        //Tulis Diskon
        if(transaksi[position].diskon!! > 0){
            printable = TextPrintable.Builder()
                .setText("-${transaksi[position].diskon!!}%")
                .setAlignment(DefaultPrinter.ALIGNMENT_RIGHT)
                .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                .setNewLinesAfter(1)
                .build()
            printables.add(printable)
        }

        //Tulis Jumlah Sub Total
        printable = TextPrintable.Builder()
            .setText("") //Sub Total
            .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .build()
        printables.add(printable)
        printable = TextPrintable.Builder()
            .setText(formatUang(transaksi[position].subTotal!!.toDouble()))
            .setAlignment(DefaultPrinter.ALIGNMENT_RIGHT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)

        //Tulis Jumlah Bayar CASH
        printable = TextPrintable.Builder()
            .setText("Bayar   :  ")
            .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .build()
        printables.add(printable)
        printable = TextPrintable.Builder()
            .setText(formatUang(transaksi[position].kembalian!!.toDouble()+transaksi[position].subTotal!!.toDouble()))
            .setAlignment(DefaultPrinter.ALIGNMENT_RIGHT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        //Tulis Jumlah Kembalian
        printable = TextPrintable.Builder()
            .setText("Kembali  :  ")
            .setAlignment(DefaultPrinter.ALIGNMENT_LEFT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .build()
        printables.add(printable)
        printable = TextPrintable.Builder()
            .setText(formatUang(transaksi[position].kembalian!!.toDouble()))
            .setAlignment(DefaultPrinter.ALIGNMENT_RIGHT)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(2)
            .build()
        printables.add(printable)

        //Tulis Terimakasih
        printable = TextPrintable.Builder()
            .setText("~ Terimakasih ~")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        //Tulis POwered By
        printable = TextPrintable.Builder()
            .setText("Powered By BIGTEK")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(1)
            .build()
        printables.add(printable)
        //Tulis POwered By
        printable = TextPrintable.Builder()
            .setText("www.bigtek.co.id")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setNewLinesAfter(5)
            .build()
        printables.add(printable)
    }

    private fun printWithPDF(){
        var license : InputStream = context.resources.openRawResource(R.raw.itextkey)

        val filename = "Struk.pdf"
        val filepath = "Struk"
        var pdfFile = File(context.getExternalFilesDir(filepath), filename)
        //generatePDF()
    }

    private fun generatePDF() {
        val filename = "Struk.pdf"
        val filepath = "Struk"
        val pdfFile = File(context.getExternalFilesDir(filepath), filename)

        val document = Document()
        val docWriter : PdfWriter = PdfWriter.getInstance(document, FileOutputStream(pdfFile))
        document.open()
        val cb : PdfContentByte = docWriter.directContent

        createHeadings(cb, 400F,780F,"Company Name");
        createHeadings(cb,400F,765F,"Address Line 1");
        createHeadings(cb,400F,750F,"Address Line 2");
        createHeadings(cb,400F,735F,"City, State - ZipCode");
        createHeadings(cb,400F,720F,"Country")

    }

    private fun createHeadings(cb : PdfContentByte, x : Float, y : Float, text : String){
        cb.beginText()
        cb.setFontAndSize(null,8F)
        cb.setTextMatrix(x,y)
        cb.showText(text.trim())
        cb.endText()

        val columnWidths = {1.5f; 2f; 5f; 2f; 2f}
        //var table : PdfPTable = PdfPTable(columnWidths)
    }

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