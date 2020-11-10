package com.estmob.android.sendanywhere.sdk.ui.example.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.estmob.android.sendanywhere.sdk.ui.example.R;

public class ButtonTextFont extends Button {

    public ButtonTextFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public ButtonTextFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public ButtonTextFont(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            @SuppressLint("CustomViewStyleable") TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MyTextView);

            Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "LitSans-Medium.otf");
            setTypeface(myTypeface);
            a.recycle();
        }
    }
}