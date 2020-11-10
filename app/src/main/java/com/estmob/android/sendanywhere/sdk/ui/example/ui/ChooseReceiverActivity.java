package com.estmob.android.sendanywhere.sdk.ui.example.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.estmob.android.sendanywhere.sdk.ui.example.AppContext;
import com.estmob.android.sendanywhere.sdk.ui.example.Constant;
import com.estmob.android.sendanywhere.sdk.ui.example.JsonHelper;
import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.common.BaseActivity;
import com.estmob.android.sendanywhere.sdk.ui.example.core.BaseTransfer;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.FileInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ToastUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.WifiMgr;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.adapter.WifiScanResultAdapter;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.view.RadarScanView;
import com.estmob.android.sendanywhere.sdk.ui.example.utils.ListUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.utils.NavigatorUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.utils.NetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mayubao on 2016/11/28.
 * Contact me 345269374@qq.com
 */
public class ChooseReceiverActivity extends BaseActivity {

    private static final String TAG = ChooseReceiverActivity.class.getSimpleName();
    /**
     * Topbar相关UI
     */
    @BindView(R.id.tv_back)
    ImageView tv_back;

    @BindView(R.id.btn_scan_qr)
    ImageView btnScanQr;
    /**
     * 其他UI
     */
    @BindView(R.id.radarView)
    RadarScanView radarScanView;
//    @Bind(R.id.tab_layout)
//    TabLayout tab_layout;
//    @Bind(R.id.view_pager)
//    ViewPager view_pager;

    /**
     * 扫描结果
     */
    @BindView(R.id.lv_result)
    ListView lv_result;

    List<ScanResult> mScanResultList;
    WifiScanResultAdapter mWifiScanResultAdapter;

    private static final int REQUEST_CODE_QR_SCAN = 5;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 10;

    Dialog mDialog ;
    /**
     * 与 文件发送方 通信的 线程
     */
    Runnable mUdpServerRuannable;


    public static final int MSG_TO_FILE_SENDER_UI = 0X88;   //消息：跳转到文件发送列表UI
    public static final int MSG_TO_SHOW_SCAN_RESULT = 0X99; //消息：更新扫描可连接Wifi网络的列表

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TO_FILE_SENDER_UI) {
                mDialog.dismiss();
               // ToastUtils.show(getContext(), "进入文件发送列表");
                NavigatorUtils.toFileSenderListUI(getContext());
                finishNormal();


            } else if (msg.what == MSG_TO_SHOW_SCAN_RESULT) {
                getOrUpdateWifiScanResult();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TO_SHOW_SCAN_RESULT), 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_receiver);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        ButterKnife.bind(this);

        init();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeSocket();

        //断开当前的Wifi网络
        WifiMgr.getInstance(getContext()).disconnectCurrentNetwork();
        mHandler.removeCallbacksAndMessages(null);
        this.finish();
    }

    /**
     * 成功进入 文件发送列表UI 调用的finishNormal()
     */
    private void finishNormal() {
        closeSocket();
        this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateUI();
                // permission was granted, yay! do the// calendar task you need to do.

            }

            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * 初始化
     */
    private void init() {

        mDialog = new Dialog(this, R.style.AppBaseTheme);
        mDialog.setContentView(R.layout.custom_fullscreen_dialog);

        radarScanView.startScan();

//        if(WifiMgr.getInstance(getContext()).isWifiEnable()){//wifi打开的情况
//        }else{//wifi关闭的情况
//            WifiMgr.getInstance(getContext()).openWifi();
//        }

        if (!WifiMgr.getInstance(getContext()).isWifiEnable()) {//wifi Not opened
            WifiMgr.getInstance(getContext()).openWifi();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant
            }else {
                updateUI();
            }
        } else {
            updateUI();
        }

        btnScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQRCodeScan();
            }
        });
        //Android 6.0 扫描wifi 需要开启定位
    }
    /**
     * 更新UI
     */
    private void updateUI() {
        getOrUpdateWifiScanResult();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TO_SHOW_SCAN_RESULT), 1000);
    }

    /**
     * 获取或者更新wifi扫描列表
     */
    private void getOrUpdateWifiScanResult() {
        WifiMgr.getInstance(getContext()).startScan();
        mScanResultList = WifiMgr.getInstance(getContext()).getScanResultList();
        //mScanResultList = ListUtils.filterWithNoPassword(mScanResultList);
        if (mScanResultList != null) {
            mWifiScanResultAdapter = new WifiScanResultAdapter(getContext(), mScanResultList);
            lv_result.setAdapter(mWifiScanResultAdapter);
            lv_result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //TODO 进入文件传输部分
                    ScanResult scanResult = mScanResultList.get(position);
                    Log.i(TAG, "###select the wifi info ======>>>" + scanResult.toString());
                    //1.Connect Network
                    String ssid = Constant.DEFAULT_SSID;
                    ssid = scanResult.SSID;

                    JsonHelper.Capabilities = scanResult.capabilities;
                    String mode = getSecurityMode(scanResult.capabilities);

                    //check if current connected SSID
                    WifiMgr.getInstance(getContext()).openWifi();
                    Log.e("mode","=>"+mode);

                    if (mode.equalsIgnoreCase("OPEN")) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                            Log.e("API29","API29");
//                            //WifiMgr.createWifiCfgOreo(getApplicationContext(), ssid, null);
//                        }else{
//                            WifiMgr.getInstance(getContext()).addNetwork(WifiMgr.createWifiCfg(ssid, null, 1  ));
//                        }
                        WifiMgr.getInstance(getContext()).addNetwork(WifiMgr.createWifiCfg(ssid, null, 1  ));
                        //2.Send UDP notification information to the file receiver, enable ServerSocketRunnable
                        mUdpServerRuannable = createSendMsgToServerRunnable(WifiMgr.getInstance(getContext()).getIpAddressFromHotspot());
                        AppContext.MAIN_EXECUTOR.execute(mUdpServerRuannable);
                    } else {
                        openQRCodeScan();
                    }
//                    //2.Send UDP notification information to the file receiver, enable ServerSocketRunnable
//                    mUdpServerRuannable = createSendMsgToServerRunnable(WifiMgr.getInstance(getContext()).getIpAddressFromHotspot());
//                    AppContext.MAIN_EXECUTOR.execute(mUdpServerRuannable);
                }
            });
        }
    }

    private void openQRCodeScan() {
        Intent i = new Intent(ChooseReceiverActivity.this, QrCodeActivity.class);
        startActivityForResult(i, REQUEST_CODE_QR_SCAN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {

            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if (result != null) {
                AlertDialog alertDialog = new AlertDialog.Builder(ChooseReceiverActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }
            return;

        }
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            try {
                JSONObject jsonObject = new JSONObject(result);
                String SSID = jsonObject.getString(JsonHelper.SSID);
                String Password = jsonObject.getString(JsonHelper.Password);
                //String BSSID = jsonObject.getString(JsonHelper.BSSID);
                String Capabilities = JsonHelper.Capabilities;
                String mode = getSecurityMode(Capabilities);
                Log.e("SSID","=>"+ SSID + "=>"+Password + "=>"+Capabilities);
                if(mode.equalsIgnoreCase("OPEN")){
                    WifiMgr.getInstance(getContext()).addNetwork(WifiMgr.createWifiCfg(SSID, null, 1  ));
                } else if (mode.equalsIgnoreCase("WEP")) {
                    WifiMgr.getInstance(getContext()).addNetwork(WifiMgr.createWifiCfg(SSID, Password, 2));
                }else{
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        Log.e("API29","API29");
//                        //WifiMgr.createWifiCfgOreo(getApplicationContext(), SSID, Password);
//                    }else{
//                        WifiMgr.getInstance(getContext()).addNetwork(WifiMgr.createWifiCfg(SSID, Password, 3));
//                    }
                    WifiMgr.getInstance(getContext()).addNetwork(WifiMgr.createWifiCfg(SSID, Password, 3));
                }
                //2.Send UDP notification information to the file receiver, enable ServerSocketRunnable

                mUdpServerRuannable = createSendMsgToServerRunnable(WifiMgr.getInstance(getContext()).getIpAddressFromHotspot());
                AppContext.MAIN_EXECUTOR.execute(mUdpServerRuannable);
                mDialog.show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Method to Get Network Security Mode
     *
     * @return OPEN PSK EAP OR WEP
     */
    private String getSecurityMode(String capabilities) {
        final String cap = capabilities;
        final String[] modes = {"WPA", "EAP", "WEP"};
        for (int i = modes.length - 1; i >= 0; i--) {
            if (cap.contains(modes[i])) {
                return modes[i];
            }
        }
        return "OPEN";
    }

    @OnClick({R.id.tv_back, R.id.radarView})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back: {
                onBackPressed();
                break;
            }
            case R.id.radarView: {
                Log.i(TAG, "radarView ------>>> click!");
                mUdpServerRuannable = createSendMsgToServerRunnable(WifiMgr.getInstance(getContext()).getIpAddressFromHotspot());
                AppContext.MAIN_EXECUTOR.execute(mUdpServerRuannable);
                break;
            }
        }
    }


    /**
     * 创建发送UDP消息到 文件接收方 的服务线程
     *
     * @param serverIP
     */
    private Runnable createSendMsgToServerRunnable(final String serverIP) {
        Log.i(TAG, "receiver serverIp ----->>>" + serverIP);
        return new Runnable() {
            @Override
            public void run() {
                try {
                    startFileSenderServer(serverIP, Constant.DEFAULT_SERVER_COM_PORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    /**
     * 开启 文件发送方 通信服务 (必须在子线程执行)
     *
     * @param targetIpAddr
     * @param serverPort
     * @throws Exception
     */
    DatagramSocket mDatagramSocket;

    private void startFileSenderServer(String targetIpAddr, int serverPort) throws Exception {
//        Thread.sleep(3*1000);
        // 确保Wifi连接上之后获取得到IP地址
        int count = 0;
        while (targetIpAddr.equals(Constant.DEFAULT_UNKOWN_IP) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(1000);
            targetIpAddr = WifiMgr.getInstance(getContext()).getIpAddressFromHotspot();
            Log.i(TAG, "receiver serverIp ----->>>" + targetIpAddr);
            count++;
        }

        // 即使获取到连接的热点wifi的IP地址也是无法连接网络 所以采取此策略
        count = 0;
        while (!NetUtils.pingIpAddress(targetIpAddr) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(500);
            Log.i(TAG, "try to ping ----->>>" + targetIpAddr + " - " + count);
            count++;
        }

        mDatagramSocket = new DatagramSocket(serverPort);
        byte[] receiveData = new byte[1024];
        byte[] sendData = null;
        InetAddress ipAddress = InetAddress.getByName(targetIpAddr);

        //0.发送 即将发送的文件列表 到文件接收方
        sendFileInfoListToFileReceiverWithUdp(serverPort, ipAddress);

        //1.发送 文件接收方 初始化
        sendData = Constant.MSG_FILE_RECEIVER_INIT.getBytes(BaseTransfer.UTF_8);
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, ipAddress, serverPort);
        mDatagramSocket.send(sendPacket);
        Log.i(TAG, "Send Msg To FileReceiver######>>>" + Constant.MSG_FILE_RECEIVER_INIT);

//        sendFileInfoListToFileReceiverWithUdp(serverPort, ipAddress);


        //2.Receive File Recipient Initialize Feedback
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            mDatagramSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), BaseTransfer.UTF_8).trim();
            Log.i(TAG, "Get the msg from FileReceiver######>>>" + response);
            if (response != null && response.equals(Constant.MSG_FILE_RECEIVER_INIT_SUCCESS)) {
                // 进入文件发送列表界面 （并且通知文件接收方进入文件接收列表界面）
                mHandler.obtainMessage(MSG_TO_FILE_SENDER_UI).sendToTarget();
            }
        }
    }

    /**
     * 发送即将发送的文件列表到文件接收方
     *
     * @param serverPort
     * @param ipAddress
     * @throws IOException
     */
    private void sendFileInfoListToFileReceiverWithUdp(int serverPort, InetAddress ipAddress) throws IOException {
        //1.1将发送的List<FileInfo> 发送给 文件接收方
        //如何将发送的数据列表封装成JSON
        Map<String, FileInfo> sendFileInfoMap = AppContext.getAppContext().getFileInfoMap();
        List<Map.Entry<String, FileInfo>> fileInfoMapList = new ArrayList<Map.Entry<String, FileInfo>>(sendFileInfoMap.entrySet());
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        //排序
        Collections.sort(fileInfoMapList, Constant.DEFAULT_COMPARATOR);
        for (Map.Entry<String, FileInfo> entry : fileInfoMapList) {
            if (entry.getValue() != null) {
                FileInfo fileInfo = entry.getValue();
                String fileInfoStr = FileInfo.toJsonStr(fileInfo);
                DatagramPacket sendFileInfoListPacket =
                        new DatagramPacket(fileInfoStr.getBytes(), fileInfoStr.getBytes().length, ipAddress, serverPort);
                try {
                    mDatagramSocket.send(sendFileInfoListPacket);
                    Log.i(TAG, "sendFileInfoListToFileReceiverWithUdp------>>>" + fileInfoStr + "=== Success!");
                } catch (Exception e) {
                    Log.i(TAG, "sendFileInfoListToFileReceiverWithUdp------>>>" + fileInfoStr + "=== Failure!");
                }

            }
        }
    }

    /**
     * 关闭UDP Socket 流
     */
    private void closeSocket() {
        if (mDatagramSocket != null) {
            if (mDatagramSocket.isConnected()) {
                mDatagramSocket.disconnect();
            }
            if (!mDatagramSocket.isClosed()) {
                mDatagramSocket.close();
            }
            mDatagramSocket = null;
        }
    }

}
