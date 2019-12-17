package com.example.pluginb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private TextView txvPlugin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pluginb);
        initView();
    }

    private void initView() {
        txvPlugin = findViewById(R.id.plugin_txv);
//        txvPlugin.setText(com.example.baselib.R.string.formlib);
        txvPlugin.setText("plugin_b");
    }
}
