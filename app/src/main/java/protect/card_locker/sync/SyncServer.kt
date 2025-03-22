package protect.card_locker.sync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import protect.card_locker.CatimaBarcode
import protect.card_locker.DBHelper
import protect.card_locker.Group
import protect.card_locker.ImageLocationType
import protect.card_locker.R
import protect.card_locker.Utils
import java.io.IOException
import java.math.BigDecimal
import java.util.Currency
import java.util.Date


class SyncServer(val context: Context) {

    companion object {
        private const val INFO_ENDPOINT = "/api/server-info"
        private const val CARD_ENDPOINT = "/api/card"
        private const val CARDS_ENDPOINT = "/api/cards"
        private const val GROUP_ENDPOINT = "/api/group"
        private const val GROUPS_ENDPOINT = "/api/groups"

        fun reach(url: String): ApiResponse {
            try {
                val client = OkHttpClient()

                val request = Request.Builder().url(url + INFO_ENDPOINT).build()
                client.newCall(request).execute().use { response ->
                    return response.body?.run {
                        Gson().fromJson(string(), ApiResponse::class.java)
                    } ?: ApiResponse(response.isSuccessful, "Unknown")
                }
            } catch (e: IOException) {
                return ApiResponse(false, e.message)
            }
        }
    }

    private val serverUrl = PreferenceManager.getDefaultSharedPreferences(context)
        .getString(context.resources.getString(R.string.pref_sync_server_url), "")
        ?.takeIf { it.isNotEmpty() }
    private val client = OkHttpClient()

    fun download() {
        if (serverUrl == null) return

        val reach = reach(serverUrl)
        if (!reach.ok) return
        val db = DBHelper(context).writableDatabase

        val groupsRequest = Request.Builder()
            .url(serverUrl + GROUPS_ENDPOINT)
            .build()
        val groupsResponse = client.newCall(groupsRequest).execute()
        val groupsBody = groupsResponse.body?.string() ?: return
        val groups = Gson().fromJson(groupsBody, Array<SyncGroup>::class.java)

        if (groups.isNotEmpty()) {
            for (group in groups) {
                DBHelper.insertGroup(db, group.id)
            }
            DBHelper.reorderGroups(db, groups.map { Group(it.id, it.orderid) })
        }

        val cardsRequest = Request.Builder()
            .url(serverUrl + CARDS_ENDPOINT)
            .build()
        val cardsResponse = client.newCall(cardsRequest).execute()
        val cardsBody = cardsResponse.body?.string() ?: return
        val cards = Gson().fromJson(cardsBody, Array<SyncCard>::class.java)
            .takeIf { it.isNotEmpty() } ?: return

        for (card in cards) {
            DBHelper.updateLoyaltyCard(
                db,
                card.id,
                card.store,
                card.note,
                card.validfrom?.run { Date(this) },
                card.expiry?.run { Date(this) },
                card.balance.run { BigDecimal(this) },
                card.balancetype?.run { Currency.getInstance(this) },
                card.cardid,
                card.barcodeId,
                card.barcodetype?.run { CatimaBarcode.fromName(this) },
                card.headercolor,
                card.starstatus,
                Date().time,
                card.archivestatus,
            )
            DBHelper.insertLoyaltyCard(
                db,
                card.id,
                card.store,
                card.note,
                card.validfrom?.run { Date(this) },
                card.expiry?.run { Date(this) },
                card.balance.run { BigDecimal(this) },
                card.balancetype?.run { Currency.getInstance(this) },
                card.cardid,
                card.barcodeId,
                card.barcodetype?.run { CatimaBarcode.fromName(this) },
                card.headercolor,
                card.starstatus,
                Date().time,
                card.archivestatus,
            )

            Utils.saveCardImage(context, card.front?.toBitmap(), card.id, ImageLocationType.front)
            Utils.saveCardImage(context, card.back?.toBitmap(), card.id, ImageLocationType.back)
            Utils.saveCardImage(context, card.icon?.toBitmap(), card.id, ImageLocationType.icon)

            if (!card.groups.isNullOrEmpty()) {
                DBHelper.setLoyaltyCardGroups(db, card.id, card.groups.map { Group(it, -1) })
            }
        }

    }

    fun upsertCard(card: SyncCard) = request(card, CARD_ENDPOINT, method = "POST")
    fun deleteCard(id: Int) = request(IDInt(id), CARD_ENDPOINT, method = "DELETE")
    fun upsertGroup(group: SyncGroup) = request(group, GROUP_ENDPOINT, method = "POST")
    fun deleteGroup(id: String) = request(IDString(id), GROUP_ENDPOINT, method = "DELETE")

    private fun request(obj: Any, endpoint: String, method: String) {
        if (serverUrl == null) return

        val reach = reach(serverUrl)
        if (!reach.ok) return

        val json = Gson().toJson(obj)
        val request = Request.Builder()
            .url(serverUrl + endpoint)
            .method(method, json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        client.newCall(request).execute()
    }

    private fun String.toBitmap(): Bitmap? {
        val encodeByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    }
}
