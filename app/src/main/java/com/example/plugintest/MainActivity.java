package com.example.plugintest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.plugintest.utils.PluginDataManager;
import com.example.plugintest.utils.Reflect;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private TextView txvPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button showPlugin = findViewById(R.id.show_plugin);
        showPlugin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });
        initView();
    }


    private void initView() {
        txvPlugin = findViewById(R.id.txv_main);
        try {
            String stringFromPluginA = Reflect.on("com.example.plugina.CodeFromA",
                    PluginDataManager.getInstance(this).getPluginInfo("plugina.apk").classLoader)
                    .create()
                    .call("methodFromA")
                    .get();
            txvPlugin.setText(stringFromPluginA);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "initView: ", e);
        }

//        try {
//            Class<?> codeFromA = mPluginInfo.classLoader.loadClass("com.example.plugina.CodeFromA");
//            Class<?> codeFromB = mPluginInfo.classLoader.loadClass("com.example.pluginb.CodeFromB");
//            Method[] methods = codeFromA.getMethods();
//            for (Method method : methods) {
//                Log.d("codeFrom", "A: " + method.getName());
//            }
//            Method[] methodb = codeFromB.getMethods();
//            for (Method method : methodb) {
//                Log.d("codeFrom", "B: " + method.getName());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(TAG, "initView: ",e );
//        }
    }
}
