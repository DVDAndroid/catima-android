package protect.card_locker.sync

data class ApiResponse(
    val ok: Boolean,
    val msg: String?,
)

data class SyncCard(
    val id: Int,
    val store: String,
    val note: String,
    val validFrom: Long?,
    val expiry: Long?,
    val balance: String,
    val balanceType: String?,
    val cardId: String,
    val barcodeId: String?,
    val barcodeType: String?,
    val headerColor: Int?,
    val starStatus: Int,
    val archiveStatus: Int,
    val groups: List<String>,
    val front: String?,
    val back: String?,
)

data class SyncGroup(
    val id: String,
    val orderId: Int,
)

data class IDString(val id: String)
data class IDInt(val id: Int)