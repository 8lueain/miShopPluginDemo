package com.example.plugintest;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.plugintest.utils.PluginDataManager;
import com.example.plugintest.utils.Reflect;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PluginDataManager.getInstance(this).generateAllResource();
    }

    protected String onGetPluginName() {
        return "";
    }

    protected Object getInstanceFromPlugin(String className, @Nullable Object... args) {
        try {
            Object o = Reflect.on(className, getClassLoader())
                    .create(args)
                    .get();
            Log.d(TAG, "getInstanceFromPlugin: [" + o + "]");
            return o;
        } catch (Reflect.ReflectException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Resources getResources() {
        PluginInfo pluginInfo = PluginDataManager.getInstance(this).getPluginInfo(onGetPluginName());
        if (null != pluginInfo) {
            return pluginInfo.resources;
        }
        return super.getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        PluginInfo pluginInfo = PluginDataManager.getInstance(this).getPluginInfo(onGetPluginName());
        if (null != pluginInfo) {
            return pluginInfo.classLoader;
        }
        return super.getClassLoader();
    }

    @Override
    public Resources.Theme getTheme() {
        PluginInfo pluginInfo = PluginDataManager.getInstance(this).getPluginInfo(onGetPluginName());
        if (null != pluginInfo) {
            return pluginInfo.theme;
        }
        return super.getTheme();
    }
}
