package protect.card_locker.sync

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import protect.card_locker.R
import java.io.IOException


class SyncServer(val context: Context) {

    companion object {
        private const val INFO_ENDPOINT = "/api/server-info"
        private const val CARD_ENDPOINT = "/api/card"
        private const val GROUP_ENDPOINT = "/api/group"

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
}
