package protect.card_locker.sync

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