package protect.card_locker.sync

import android.content.Context
import protect.card_locker.async.CompatCallable

class SyncUpsertGroupCompatCallable(
    val context: Context,
    val group: SyncGroup,
) : CompatCallable<Unit> {
    override fun onPostExecute(result: Any?) {

    }

    override fun onPreExecute() {

    }

    override fun call() {
        SyncServer(context).upsertGroup(group)
    }
}
