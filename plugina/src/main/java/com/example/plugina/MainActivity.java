package com.example.plugina;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements PlusOneFragment.OnFragmentInteractionListener {
    private TextView txvPlugin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        txvPlugin = findViewById(R.id.plugin_txv);
//        txvPlugin.setText(com.example.baselib.R.string.formlib);
        txvPlugin.setText(R.string.string_form_lib);
//        txvPlugin.setText(com.wx.goodview.R.color.highlighted_text_material_dark);
//        txvPlugin.setText(com.example.baselib.R.color.highlighted_text_material_dark);
        getSupportFragmentManager().beginTransaction().add(R.id.container_plugina_aty, new SimpleFragment()).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
