package com.estmob.android.sendanywhere.sdk.ui.example;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TextFont extends androidx.appcompat.widget.AppCompatTextView {

    public TextFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public TextFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public TextFont(Context context) {
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