package id.co.bigtek.kasir.model.database

class ModelProduk(val kode: String,
                  val nama: String,
                  val harga: Double,
                  val harga_jual: Double,
                  val stok : Int,
                  val diskon : Int,
                  val kategori_id : Int)