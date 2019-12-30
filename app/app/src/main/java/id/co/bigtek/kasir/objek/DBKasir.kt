package id.co.bigtek.kasir.objek

import android.provider.BaseColumns

object DBKasir {

    class TabelProduk : BaseColumns {
        companion object {
            val NAMA_TABEL = "produk"
            val PRODUK_KODE = "produk_kode"
            val PRODUK_NAMA = "produk_nama"
            val PRODUK_HARGA = "produk_harga_pokok"
            val PRODUK_HARGA_JUAL = "produk_harga_jual"
            val PRODUK_STOK = "produk_stok"
            val PRODUK_DISKON = "produk_diskon"
            val KATEGORI_ID = "kategori_id"
        }
    }

    class TabelTransaksi : BaseColumns{
        companion object{
            val NAMA_TABEL = "transaksi"
            val TRANSAKSI_KODE = "transaksi_kode"
            val TRANSAKSI_TGL = "transaksi_tanggal"
            val TRANSAKSI_TOTAL = "transaksi_total"
            val TRANSAKSI_SUB_TOTAL = "transaksi_sub_total"
            val TRANSAKSI_JML_BAYAR = "transaksi_bayar"
            val TRANSAKSI_KEMBALIAN = "transaksi_kembalian"
            val TRANSAKSI_DISKON = "transaksi_diskon"
        }
    }

    class TabelDetailTransaksi : BaseColumns{
        companion object{
            val NAMA_TABEL = "detail_transaksi"
            val DETAIL_KODE = "detail_kode"
            val TRANSAKSI_KODE = "transaksi_kode"
            val PRODUK_KODE = "produk_kode"
            val DETAIL_QTY = "detail_qty"
        }
    }

    class TabelKategori : BaseColumns{
        companion object{
            val NAMA_TABEL = "kategori"
            val KATEGORI_ID = "kategori_id"
            val KATEGORI_PRODUK = "kategori_produk"
        }
    }
}