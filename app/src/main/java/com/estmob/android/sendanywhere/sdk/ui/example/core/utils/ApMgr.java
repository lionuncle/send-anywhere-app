package com.estmob.android.sendanywhere.sdk.ui.example.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.wifi.WifiConfiguration;

import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.estmob.android.sendanywhere.sdk.ui.example.JsonHelper;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * 1.Prepare
 * use WifiManager class need the permission
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * <p>
 * 2.Usage
 * ...start
 * ApMgr.isApOn(Context context); // check Ap state :boolean
 * ApMgr.configApState(Context context); // change Ap state :boolean
 * ...end
 * <p>
 * <p>
 * Created by mayubao on 2016/11/2.
 * Contact me 345269374@qq.com
 */
public class ApMgr {

    static CallBackListener callBackListener;

    public void addListener(CallBackListener callBackListener) {
        this.callBackListener = callBackListener;
    }

    private static void generateQRCode(Context context, String SSID, String preSharedKey) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(preSharedKey, null, QRGContents.Type.TEXT, smallerDimension);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();

            if (callBackListener != null) {
                Log.e(TAG, "generateQRCodeApmrg: "+ SSID );
                callBackListener.QRCodeGenerated(SSID, bitmap);
            }
            // Setting Bitmap to ImageView
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    //check whether wifi hotspot on or off
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable ignored) {
        }
        return false;
    }

    //close wifi hotspot
    public static void disableAp(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            turnOffHotspot();
        } else {
            WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            try {
                Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method.invoke(wifimanager, null, false);
            } catch (Throwable ignored) {
            }
        }
    }

    private static WifiManager.LocalOnlyHotspotReservation mReservation;

    // toggle wifi hotspot on or off, and specify the hotspot name
    public static boolean configApState(Context context, String apName) {
        if (callBackListener != null) {
            callBackListener.showQRCodeProgressbar();
        }
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration =  new WifiConfiguration();;
        try {
            wificonfiguration.SSID = apName;

            Log.e("wificonfiguration","=>"+wificonfiguration.SSID);
            // if WiFi is on, turn it off
            if (isApOn(context)) {
                wifimanager.setWifiEnabled(false);
                // if ap is on and then disable ap
                disableAp(context);
            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                wifimanager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                        super.onStarted(reservation);
                        Log.d(TAG, "Wifi Hotspot is on now");
                        mReservation = reservation;

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(JsonHelper.SSID, reservation.getWifiConfiguration().SSID);
                            jsonObject.put(JsonHelper.Password, reservation.getWifiConfiguration().preSharedKey);


                            generateQRCode(context,reservation.getWifiConfiguration().SSID, jsonObject.toString());
                            Log.e(TAG, "Apmgr: " + reservation.getWifiConfiguration().SSID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onStopped() {
                        super.onStopped();
                        Log.d(TAG, "onStopped: ");
                    }

                    @Override
                    public void onFailed(int reason) {
                        super.onFailed(reason);
                        Log.d(TAG, "onFailed: ");
                    }
                }, new Handler());

            } else {
                wificonfiguration.preSharedKey = getSSID();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonHelper.SSID, wificonfiguration.SSID);
                jsonObject.put(JsonHelper.Password, wificonfiguration.preSharedKey);

                generateQRCode(context, wificonfiguration.SSID, jsonObject.toString());
                Log.e("TAG", "Wifi Hotspot is on now");
                Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method.invoke(wifimanager, wificonfiguration, !isApOn(context));
                return true;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }

    protected static String getSSID() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 13) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }
} // end of class
