package protect.card_locker.sync

import android.content.Context
import protect.card_locker.async.CompatCallable

class SyncDeleteCardCompatCallable(
    val context: Context,
    val id: Int,
) : CompatCallable<Unit> {
    override fun onPostExecute(result: Any?) {

    }

    override fun onPreExecute() {

    }

    override fun call() {
        SyncServer(context).deleteCard(id)
    }
}