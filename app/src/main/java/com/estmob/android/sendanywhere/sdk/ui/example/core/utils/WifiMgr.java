package com.estmob.android.sendanywhere.sdk.ui.example.core.utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.NetworkCapabilities;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
//import android.net.wifi.WifiNetworkSpecifier;
//import android.net.NetworkRequest;
//import android.net.ConnectivityManager.NetworkCallback;
//import android.net.ConnectivityManager;
//import android.net.NetworkSpecifier;
//import android.net.Network;
//import android.os.Build;
import android.util.Log;
//import android.os.PatternMatcher;
//import android.net.MacAddress;
//
//import androidx.annotation.RequiresApi;

import java.util.List;

/**
 * Created by mayubao on 2016/11/2.
 * Contact me 345269374@qq.com
 */
public class WifiMgr {

    /**
     * Create the type of WifiConfiguration
     */
    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WEP = 2;
    public static final int WIFICIPHER_WPA = 3;


    private static WifiMgr mWifiMgr;

    private Context mContext;
    private WifiManager mWifiManager;

    //scan the result
    List<ScanResult> mScanResultList;
    List<WifiConfiguration> mWifiConfigurations;


    //current wifi configuration info
    WifiInfo mWifiInfo;

    private WifiMgr(Context context){
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiMgr getInstance(Context context){
        if(mWifiMgr == null){
            synchronized (WifiMgr.class){
                if(mWifiMgr == null){
                    mWifiMgr = new WifiMgr(context);
                }
            }
        }

        return mWifiMgr;
    }

    /**
     * 打开wifi
     */
    public void openWifi(){
        if(!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(true);
        }
    }


    /**
     * 关闭wifi
     */
    public void closeWifi(){
        if(mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(false);
        }
    }


    /**
     * 判断wifi是否开启的状态
     * @return
     */
    public boolean isWifiEnable(){
        return mWifiManager == null ? false : mWifiManager.isWifiEnabled();
    }


    /**
     * wifi扫描
     */
    public void startScan(){
        mWifiManager.startScan();
        mScanResultList = mWifiManager.getScanResults();
        //mWifiConfigurations = mWifiManager.getConfiguredNetworks();
    }

    public List<ScanResult> getScanResultList() {
        return mScanResultList;
    }

    public List<WifiConfiguration> getWifiConfigurations() {
        return mWifiConfigurations;
    }


    /**
     * 添加到指定Wifi网络 /切换到指定Wifi网络
     * @param wf
     * @return
     */
    public boolean addNetwork(WifiConfiguration wf){
        //断开当前的连接
        disconnectCurrentNetwork();

        //连接新的连接
        int netId = mWifiManager.addNetwork(wf);
        boolean enable = mWifiManager.enableNetwork(netId, true);
        return enable;
    }

    /**
     * 关闭当前的Wifi网络
     * @return
     */
    public boolean disconnectCurrentNetwork(){
        if(mWifiManager != null && mWifiManager.isWifiEnabled()){
            int netId = mWifiManager.getConnectionInfo().getNetworkId();
            mWifiManager.disableNetwork(netId);
            return mWifiManager.disconnect();
        }
        return false;
    }

    /**
     * 创建WifiConfiguration
     *
     * @param ssid
     * @param password
     * @param type
     * @return
     */
//    public static WifiConfiguration createWifiCfg(String ssid, String password, int type ){
//        WifiConfiguration config = new WifiConfiguration();
//        config.allowedAuthAlgorithms.clear();
//        config.allowedGroupCiphers.clear();
//        config.allowedKeyManagement.clear();
//        config.allowedPairwiseCiphers.clear();
//        config.allowedProtocols.clear();
//
//        config.SSID = "\"" + ssid + "\"";
//
//        if(type == WIFICIPHER_NOPASS){
////            config.wepKeys[0] = "";
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
////            config.wepTxKeyIndex = 0;
//
////            无密码连接WIFI时，连接不上wifi，需要注释两行代码
////            config.wepKeys[0] = "";
////            config.wepTxKeyIndex = 0;
//        }else if(type == WIFICIPHER_WEP){
//            config.hiddenSSID = true;
//            config.wepKeys[0]= "\""+password+"\"";
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
//        }else if(type == WIFICIPHER_WPA){
//            config.preSharedKey = "\""+password+"\"";
//            config.hiddenSSID = true;
////            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
////            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
////            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
////            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
////            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
////            config.status = WifiConfiguration.Status.ENABLED;
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//            config.status = WifiConfiguration.Status.ENABLED;
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//            config.allowedPairwiseCiphers
//                    .set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedPairwiseCiphers
//                    .set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//        }
//
//        return config;
//    }

    public static WifiConfiguration createWifiCfg(String ssid, String password, int type ){


            WifiConfiguration config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();

            config.SSID = "\"" + ssid + "\"";


            if (type == WIFICIPHER_NOPASS) {
                //            config.wepKeys[0] = "";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                //            config.wepTxKeyIndex = 0;

                //            无密码连接WIFI时，连接不上wifi，需要注释两行代码
                //            config.wepKeys[0] = "";
                //            config.wepTxKeyIndex = 0;
            } else if (type == WIFICIPHER_WEP) {
                config.hiddenSSID = true;
                config.wepKeys[0] = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
            } else if (type == WIFICIPHER_WPA) {
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                //            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                //            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                //            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                //            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                //            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                //            config.status = WifiConfiguration.Status.ENABLED;
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.status = WifiConfiguration.Status.ENABLED;
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            }

            return config;


    }




//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    public static void createWifiCfgOreo(Context mContext, String ssid, String password) {
//        WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
//        builder.setSsid(ssid);
//
////        builder.setSsidPattern(new PatternMatcher("test", PatterMatcher.PATTERN_PREFIX));
////        builder.setBssidPattern(MacAddress.fromString("10:03:23:00:00:00"),
////                        MacAddress.fromString("ff:ff:ff:00:00:00"));
//        //builder.setWpa2Passphrase(password);
//        WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();
//        NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();
//        networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
//
//
//        networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);
//        NetworkRequest nr = networkRequestBuilder1.build();
//        ConnectivityManager cm = (ConnectivityManager)
//                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        ConnectivityManager.NetworkCallback networkCallback = new
//                ConnectivityManager.NetworkCallback() {
//                    @Override
//                    public void onAvailable(Network network) {
//                        super.onAvailable(network);
//                        Log.e("TAG","onAvailable" + network);
//                        //Log.d(TAG, "onAvailable:" + network);
//                        cm.bindProcessToNetwork(network);
//                    }
//
//
//                };
//
//        cm.requestNetwork(nr, networkCallback);
//    }

    /**
     * 获取当前WifiInfo
     * @return
     */
    public WifiInfo getWifiInfo(){
        mWifiInfo = mWifiManager.getConnectionInfo();
        return mWifiInfo;
    }

    /**
     * 获取当前Wifi所分配的Ip地址
     * @return
     */
//  when connect the hotspot, is still returning "0.0.0.0".
    public String getCurrentIpAddress(){
        String ipAddress = "";
        int address= mWifiManager.getDhcpInfo().ipAddress;
        ipAddress = ((address & 0xFF)
                + "." + ((address >> 8) & 0xFF)
                + "." + ((address >> 16) & 0xFF)
                + "." + ((address >> 24) & 0xFF));
        return ipAddress;
    }


    /**
     * 设备连接Wifi之后， 设备获取Wifi热点的IP地址
     * @return
     */
    public String getIpAddressFromHotspot(){
        // WifiAP ip address is hardcoded in Android.
        /* IP/netmask: 192.168.43.1/255.255.255.0 */
        String ipAddress = "192.168.43.1";
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        int address = dhcpInfo.gateway;
        Log.e("adress","=>"+address);
        ipAddress = ((address & 0xFF)
                + "." + ((address >> 8) & 0xFF)
                + "." + ((address >> 16) & 0xFF)
                + "." + ((address >> 24) & 0xFF));
        return ipAddress;
    }


    /**
     * 开启热点之后，获取自身热点的IP地址
     * @return
     */
    public String getHotspotLocalIpAddress(){
        // WifiAP ip address is hardcoded in Android.
        /* IP/netmask: 192.168.43.1/255.255.255.0 */
        String ipAddress = "192.168.43.1";
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        int address = dhcpInfo.serverAddress;
        ipAddress = ((address & 0xFF)
                + "." + ((address >> 8) & 0xFF)
                + "." + ((address >> 16) & 0xFF)
                + "." + ((address >> 24) & 0xFF));
        return ipAddress;
    }



    /**
     * 关闭Wifi
     */
    public void disableWifi(){
        if(mWifiManager != null){
            mWifiManager.setWifiEnabled(false);
        }
    }


}
