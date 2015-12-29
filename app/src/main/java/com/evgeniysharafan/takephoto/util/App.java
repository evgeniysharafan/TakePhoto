package com.evgeniysharafan.takephoto.util;

import android.app.Application;

import com.evgeniysharafan.takephoto.BuildConfig;
import com.evgeniysharafan.utils.Utils;

public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.init(this, BuildConfig.DEBUG);
    }

}
