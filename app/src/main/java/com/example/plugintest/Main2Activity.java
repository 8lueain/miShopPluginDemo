package com.example.plugintest;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.plugintest.dummy.DummyContent;
import com.example.plugintest.utils.Reflect;

public class Main2Activity extends BaseActivity implements ItemFragment.OnListFragmentInteractionListener {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(getContentView());
            Fragment fragment = (Fragment) getInstanceFromPlugin("com.example.plugina.SimpleFragment");
//            Fragment fragment = Reflect.on("com.example.plugina.ItemFragment", getClassLoader())
//                    .call("newInstance", 50)
//                    .get();

            getSupportFragmentManager().beginTransaction().add(android.R.id.primary, fragment).commit();
//            mTextView.setText(s);
        } catch (Reflect.ReflectException e) {
            e.printStackTrace();
        }
    }

    protected View getContentView() {
        FrameLayout viewRoot = new FrameLayout(this);
        viewRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mTextView = new TextView(this);
        viewRoot.addView(mTextView);
        viewRoot.setId(android.R.id.primary);
        return viewRoot;
    }

    @Override
    protected String onGetPluginName() {
        return "plugina.apk";
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
