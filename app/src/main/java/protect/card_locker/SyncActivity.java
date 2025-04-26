package protect.card_locker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import protect.card_locker.async.CompatCallable;
import protect.card_locker.async.TaskHandler;
import protect.card_locker.databinding.SyncActivityBinding;
import protect.card_locker.sync.ApiResponse;
import protect.card_locker.sync.SyncServer;

public class SyncActivity extends CatimaAppCompatActivity {

    final private TaskHandler mTasks = new TaskHandler();
    private SyncActivityBinding binding;
    private Button connectButton;
    private TextView lastSyncTextView;
    private FloatingActionButton fabSync;
    private TextInputLayout serverUrlInputLayout;
    private TextInputEditText serverUrlInputEditText;
    private SharedPreferences prefs;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SyncActivityBinding.inflate(getLayoutInflater());
        setTitle(R.string.sync);
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        enableToolbarBackButton();

        progressDialog = new ProgressDialog(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        connectButton = binding.connectButton;
        lastSyncTextView = binding.lastSync;
        fabSync = binding.fabSync;
        serverUrlInputLayout = binding.serverUrl;
        serverUrlInputEditText = binding.serverUrlEdit;

        String serverUrl = prefs.getString(getString(R.string.pref_sync_server_url), "");
        if (!serverUrl.isEmpty()) {
            serverUrlInputEditText.setText(serverUrl);
            serverUrlInputLayout.setError(null);
        } else {
            serverUrlInputLayout.setError(getString(R.string.sync_server_url_error));
            fabSync.setVisibility(View.GONE);
        }

        serverUrlInputEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                super.onTextChanged(cs, start, before, count);

                String s = cs.toString();
                if (s.isEmpty() || !(s.startsWith("http://") || s.startsWith("https://"))) {
                    serverUrlInputLayout.setError(getString(R.string.sync_server_url_error));
                    fabSync.setVisibility(View.GONE);
                } else {
                    serverUrlInputLayout.setError(null);
                }
            }
        });

        connectButton.setOnClickListener((v) -> {
            String url = serverUrlInputEditText.getText().toString();
            if (url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
                serverUrlInputLayout.setError(getString(R.string.sync_server_url_error));
                return;
            }
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            prefs.edit().putString(getString(R.string.pref_sync_server_url), url).apply();

            mTasks.executeTask(TaskHandler.TYPE.SYNC, new CompatCallable<ApiResponse>() {
                @Override
                public void onPostExecute(Object result) {
                    progressDialog.dismiss();
                    ApiResponse response = (ApiResponse) result;

                    if (response.getOk()) {
                        serverUrlInputLayout.setError(null);
                        fabSync.setVisibility(View.VISIBLE);
                    } else {
                        serverUrlInputLayout.setError(getString(R.string.sync_server_url_error) + " " + response.getMsg());
                        fabSync.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onPreExecute() {
                    progressDialog.show();
                }

                @Override
                public ApiResponse call() throws Exception {
                    return SyncServer.Companion.reach(url);
                }
            });
        });

        lastSyncTextView.setText(getString(R.string.sync_lastsync, prefs.getString(getString(R.string.pref_sync_lastsync), "never")));

        fabSync.setOnClickListener((v) -> {
            mTasks.executeTask(TaskHandler.TYPE.SYNC, new CompatCallable<Void>() {
                @Override
                public void onPostExecute(Object result) {
                    progressDialog.dismiss();

                }

                @Override
                public void onPreExecute() {
                    progressDialog.show();
                }

                @Override
                public Void call() throws Exception {
                    new SyncServer(getApplicationContext()).download();
                    new SyncServer(getApplicationContext()).downloadStoreMetadata("IT"); // TODO
                    return null;
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
