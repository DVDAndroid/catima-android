package protect.card_locker.sync

import android.util.Base64

data class ApiResponse(
    val ok: Boolean,
    val msg: String?,
)

data class SyncCard(
    val id: Int,
    val store: String,
    val note: String,
    val validfrom: Long?,
    val expiry: Long?,
    val balance: String,
    val balancetype: String?,
    val cardid: String,
    val barcodeId: String?,
    val barcodetype: String?,
    val headercolor: Int?,
    val starstatus: Int,
    val archivestatus: Int,
    val groups: List<String>?,
    val front: String?,
    val back: String?,
    val icon: String?,
)

data class SyncGroup(
    val id: String,
    val orderid: Int,
)

data class IDString(val id: String)
data class IDInt(val id: Int)

data class SyncStoreMetadata(
    val id: String,
    val name: String,
    val barcodeFormat: String,
    val regions: String,
    val website: String,
    val topStore: Boolean,
    val logo: String,
) {
    // logo base64 to byte array
    fun logoByteArray(): ByteArray? {
        return try {
            val base64 = logo.split(",").last()
            Base64.decode(base64, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
}