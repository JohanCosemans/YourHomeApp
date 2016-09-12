package net.yourhome.app.net.discovery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;

import net.yourhome.app.R;

final public class ActivityMain extends Activity {

    public final static String TAG = "ActivityMain";
    public static final String PKG = "info.lamatricexiste.network";
    public static SharedPreferences prefs = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.discovery_main_activity);
        final Context ctxt = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);

        startActivity(new Intent(ctxt, DiscoveryActivity.class));
        finish();
    }
}
