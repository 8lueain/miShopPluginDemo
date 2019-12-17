package com.example.plugina.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.example.plugina.R;

public class SimplePluginCardView extends View {
    public SimplePluginCardView(Context context) {
        this(context, null);
    }

    public SimplePluginCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePluginCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
