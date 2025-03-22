package protect.card_locker.sync

import android.content.Context
import protect.card_locker.async.CompatCallable

class SyncUpsertCardCompatCallable(
    val context: Context,
    val card: SyncCard,
) : CompatCallable<Unit> {
    override fun onPostExecute(result: Any?) {

    }

    override fun onPreExecute() {

    }

    override fun call() {
        SyncServer(context).upsertCard(card)
    }
}