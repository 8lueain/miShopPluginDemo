package com.example.plugintest;

import android.app.Application;
import com.example.baselib.utils.Utils;

import java.io.IOException;

public class HostApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Utils.copyAllAssertToCacheFolder(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
