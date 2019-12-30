package id.co.bigtek.kasir.model.database

class ModelTransaksi(val kode: Int,
                     val tanggal: String,
                     val total: Double,
                     val subtotal: Double,
                     val jml_bayar : Double,
                     val jml_kembalian : Double,
                     val diskon : Int
)