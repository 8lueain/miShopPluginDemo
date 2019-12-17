package com.example.plugina;

import android.content.Context;

public class CodeFromA {
    public String methodFromA() {
        return "code from a";
    }


    public String getResourceString(Context context) {
        return context.getResources().getString(R.string.aa_from_plugina);
    }
    public int getPluginString(Context context) {
        return R.string.aa_from_plugina;
    }

    public int getLayoutId(){
        return R.layout.fragment_plugin_simple;
    }

    public static String getStaticString () {
        return "static string form plugina";
    }
}
