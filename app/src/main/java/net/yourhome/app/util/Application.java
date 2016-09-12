package net.yourhome.app.util;

import android.content.Context;
import android.support.multidex.MultiDex;

public final class Application extends android.support.multidex.MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.replaceFont("MONOSPACE", Configuration.getInstance().getApplicationFont(getAssets()));
    }
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}