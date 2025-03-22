package protect.card_locker.sync

import android.app.ProgressDialog
import android.content.Context
import protect.card_locker.async.CompatCallable

class SyncDownloadCompatCallable(
    val context: Context,
) : CompatCallable<Unit> {
    private val progressDialog = ProgressDialog(context)
    override fun onPreExecute() {
        progressDialog.show()
    }

    override fun onPostExecute(result: Any?) {
        progressDialog.dismiss()
    }

    override fun call() {
        SyncServer(context).download()
    }
}