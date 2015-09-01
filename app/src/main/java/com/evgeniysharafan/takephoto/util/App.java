package com.evgeniysharafan.takephoto.util;

import android.app.Application;

import com.evgeniysharafan.takephoto.BuildConfig;
import com.evgeniysharafan.takephoto.util.lib.Utils;

public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.init(this, BuildConfig.DEBUG);
    }

}
