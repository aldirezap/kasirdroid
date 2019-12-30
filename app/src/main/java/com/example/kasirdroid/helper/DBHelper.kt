package com.example.kasirdroid.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kasirdroid.model.database.ModelDetailTransaksi
import com.example.kasirdroid.model.database.ModelKategori
import com.example.kasirdroid.model.database.ModelProduk
import com.example.kasirdroid.model.database.ModelTransaksi
import com.example.kasirdroid.objek.DBKasir
class DBHelper(contex:Context) : SQLiteOpenHelper(contex,DATABASE_NAME,null, DATABASE_VERSION) {
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    //Cutom Query
    fun customQuery(query : String) : Cursor?{
        val db = writableDatabase
        val cursor = db.rawQuery(query,null)
        cursor.moveToFirst()
        return cursor
    }

    @Throws(SQLiteConstraintException::class)
    fun insertProduk(produk : ModelProduk):Boolean{
        val db = writableDatabase

        val values = ContentValues()
        values.put(DBKasir.TabelProduk.PRODUK_KODE, produk.kode)
        values.put(DBKasir.TabelProduk.PRODUK_NAMA, produk.nama.replace("'",""))
        values.put(DBKasir.TabelProduk.PRODUK_HARGA, produk.harga)
        values.put(DBKasir.TabelProduk.PRODUK_HARGA_JUAL, produk.harga_jual)
        values.put(DBKasir.TabelProduk.PRODUK_STOK, produk.stok)
        values.put(DBKasir.TabelProduk.PRODUK_DISKON, produk.diskon)
        values.put(DBKasir.TabelProduk.KATEGORI_ID, produk.kategori_id)

        db.insert(DBKasir.TabelProduk.NAMA_TABEL, null, values)
        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun insertTransaksi(transaksi: ModelTransaksi):Boolean{
        val db = writableDatabase

        val values = ContentValues()
        values.put(DBKasir.TabelTransaksi.TRANSAKSI_KODE, transaksi.kode)
        values.put(DBKasir.TabelTransaksi.TRANSAKSI_TGL, transaksi.tanggal)
        values.put(DBKasir.TabelTransaksi.TRANSAKSI_TOTAL, transaksi.total)
        values.put(DBKasir.TabelTransaksi.TRANSAKSI_SUB_TOTAL, transaksi.subtotal)
        values.put(DBKasir.TabelTransaksi.TRANSAKSI_JML_BAYAR, transaksi.jml_bayar)
        values.put(DBKasir.TabelTransaksi.TRANSAKSI_KEMBALIAN, transaksi.jml_kembalian)
        values.put(DBKasir.TabelTransaksi.TRANSAKSI_DISKON, transaksi.diskon)

        db.insert(DBKasir.TabelTransaksi.NAMA_TABEL, null, values)
        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun insertDetailTransaksi(detailTransaksi: ModelDetailTransaksi):Boolean{
        val db = writableDatabase

        val values = ContentValues()
        values.put(DBKasir.TabelDetailTransaksi.DETAIL_KODE, detailTransaksi.kodeDetailTransaksi)
        values.put(DBKasir.TabelDetailTransaksi.TRANSAKSI_KODE, detailTransaksi.kodeTransaksi)
        values.put(DBKasir.TabelDetailTransaksi.PRODUK_KODE, detailTransaksi.kodeProduk)
        values.put(DBKasir.TabelDetailTransaksi.DETAIL_QTY, detailTransaksi.qty)

        db.insert(DBKasir.TabelDetailTransaksi.NAMA_TABEL, null, values)
        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun insertKategori(kategori: ModelKategori):Boolean{
        val db = writableDatabase

        val values = ContentValues()
        values.put(DBKasir.TabelKategori.KATEGORI_ID, kategori.kode)
        values.put(DBKasir.TabelKategori.KATEGORI_PRODUK, kategori.nama)

        db.insert(DBKasir.TabelKategori.NAMA_TABEL, null, values)
        return true
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(BUAT_TABEL_PRODUK)
        db.execSQL(BUAT_TABEL_TRANSAKSI)
        db.execSQL(BUAT_TABEL_DETAIL_TRANSAKSI)
        db.execSQL(BUAT_TABEL_KATEGORI)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "kasirdroid"

        private val BUAT_TABEL_PRODUK =
            "CREATE TABLE IF NOT EXISTS " + DBKasir.TabelProduk.NAMA_TABEL + " (" +
                    DBKasir.TabelProduk.PRODUK_KODE + " TEXT PRIMARY KEY," +
                    DBKasir.TabelProduk.PRODUK_NAMA + " TEXT," +
                    DBKasir.TabelProduk.PRODUK_HARGA + " REAL,"+
                    DBKasir.TabelProduk.PRODUK_HARGA_JUAL +" REAL, "+
                    DBKasir.TabelProduk.PRODUK_STOK +" INTEGER, "+
                    DBKasir.TabelProduk.PRODUK_DISKON +" INTEGER,"+
                    DBKasir.TabelProduk.KATEGORI_ID+" INTEGER)"

        private val BUAT_TABEL_TRANSAKSI =
            "CREATE TABLE IF NOT EXISTS " + DBKasir.TabelTransaksi.NAMA_TABEL + " (" +
                    DBKasir.TabelTransaksi.TRANSAKSI_KODE + " INTEGER PRIMARY KEY," +
                    DBKasir.TabelTransaksi.TRANSAKSI_TGL + " TEXT," +
                    DBKasir.TabelTransaksi.TRANSAKSI_TOTAL +" REAL, "+
                    DBKasir.TabelTransaksi.TRANSAKSI_SUB_TOTAL +" REAL, "+
                    DBKasir.TabelTransaksi.TRANSAKSI_JML_BAYAR +" REAL, "+
                    DBKasir.TabelTransaksi.TRANSAKSI_KEMBALIAN +" REAL,"+
                    DBKasir.TabelTransaksi.TRANSAKSI_DISKON +" INTEGER)"

        private val BUAT_TABEL_DETAIL_TRANSAKSI =
            "CREATE TABLE IF NOT EXISTS " + DBKasir.TabelDetailTransaksi.NAMA_TABEL + " (" +
                    DBKasir.TabelDetailTransaksi.DETAIL_KODE + " INTEGER PRIMARY KEY," +
                    DBKasir.TabelDetailTransaksi.TRANSAKSI_KODE + " INTEGER," +
                    DBKasir.TabelDetailTransaksi.PRODUK_KODE + " INTEGER,"+
                    DBKasir.TabelDetailTransaksi.DETAIL_QTY +" INTEGER)"

        private val BUAT_TABEL_KATEGORI =
            "CREATE TABLE IF NOT EXISTS " + DBKasir.TabelKategori.NAMA_TABEL + " (" +
                    DBKasir.TabelKategori.KATEGORI_ID + " INTEGER PRIMARY KEY," +
                    DBKasir.TabelKategori.KATEGORI_PRODUK + " TEXT)"

    }
}