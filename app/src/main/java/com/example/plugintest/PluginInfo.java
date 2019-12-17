package com.example.plugintest;

import android.content.res.Resources;
import dalvik.system.DexClassLoader;

public class PluginInfo {
    public String plugin_zip_path;
    public DexClassLoader classLoader;
    public Resources resources;
    public Resources.Theme theme;
}
