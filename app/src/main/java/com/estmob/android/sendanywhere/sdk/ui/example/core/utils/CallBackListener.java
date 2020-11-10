package com.estmob.android.sendanywhere.sdk.ui.example.core.utils;

import android.graphics.Bitmap;

public interface CallBackListener {
    void QRCodeGenerated(String SSID, Bitmap bitmap);

    void showQRCodeProgressbar();

    void hideProgress();
}
