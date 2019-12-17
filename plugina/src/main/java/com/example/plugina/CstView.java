package com.example.plugina;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CstView extends FrameLayout {
    public CstView(Context context) {
        this(context, null);
    }

    public CstView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CstView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }
}
