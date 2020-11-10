package com.estmob.android.sendanywhere.sdk.ui.example.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estmob.android.sendanywhere.sdk.ui.example.AppContext;
import com.estmob.android.sendanywhere.sdk.ui.example.Constant;
import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.common.BaseActivity;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.FileInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.IpPortInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.receiver.WifiAPBroadcastReceiver;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ApMgr;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.CallBackListener;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.TextUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ToastUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.WifiMgr;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.view.RadarLayout;
import com.estmob.android.sendanywhere.sdk.ui.example.utils.NavigatorUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 接收等待文件传输UI
 */
public class ReceiverWaitingActivity extends BaseActivity implements CallBackListener {

    private static final String TAG = ReceiverWaitingActivity.class.getSimpleName();

    /**
     * Android 6.0 modify wifi status need this permission: android.permission.WRITE_SETTINGS
     */
    /**
     * Topbar相关UI
     */
    @BindView(R.id.tv_back)
    ImageView tv_back;

    /**
     * 其他UI
     */
    @BindView(R.id.radarLayout)
    RadarLayout radarLayout;
    @BindView(R.id.tv_device_name)
    TextView tv_device_name;
    @BindView(R.id.tv_desc)
    TextView tv_desc;

    WifiAPBroadcastReceiver mWifiAPBroadcastReceiver;
    boolean mIsInitialized = false;
    @BindView(R.id.lay_progress)
    RelativeLayout layProgress;
    @BindView(R.id.txt_messsage)
    TextView txtMesssage;

    @BindView(R.id.img_qrcode)
    ImageView imgQrcode;

    @BindView(R.id.laycontent)
    RelativeLayout layContent;
    private String SSID;
    private Bitmap bitmap;

    @BindView(R.id.tvbarcode)
    TextView tvBarcode;
    /**
     * 与 文件发送方 通信的 线程
     */
    Runnable mUdpServerRuannable;

    public static final int MSG_TO_FILE_RECEIVER_UI = 0X88;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TO_FILE_RECEIVER_UI) {
                IpPortInfo ipPortInfo = (IpPortInfo) msg.obj;
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constant.KEY_IP_PORT_INFO, ipPortInfo);
                NavigatorUtils.toFileReceiverListUI(getContext(), bundle);

                finishNormal();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_waiting);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        ButterKnife.bind(this);

        /**
         if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_SETTINGS)
         != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, REQUEST_CODE_WRITE_SETTINGS);
         }else{
         //            initData();//初始化数据
         init();
         }
         */
        ApMgr apMgr = new ApMgr();
        apMgr.addListener(this);
        init();

    }

    @Override
    public void QRCodeGenerated(String SSID, Bitmap bitmap) {
        this.SSID = SSID;
        this.bitmap = bitmap;
        showQRCode(SSID, bitmap);
        Log.e(TAG, "showQRCodeProgressbar: " + SSID);
    }

    private void showQRCode(String SSID, Bitmap bitmap) {
        layContent.setVisibility(View.VISIBLE);
        imgQrcode.setImageBitmap(bitmap);
        tvBarcode.setText("Current Network: "+SSID);
        hideProgress();
    }

    @Override
    public void showQRCodeProgressbar() {
        Log.d("Show QR code", true + "");
        layProgress.setVisibility(View.VISIBLE);
        txtMesssage.setText(getString(R.string.txt_generating));

    }

    @Override
    public void hideProgress() {
        layProgress.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mWifiAPBroadcastReceiver != null) {
            unregisterReceiver(mWifiAPBroadcastReceiver);
            mWifiAPBroadcastReceiver = null;
        }
        closeSocket();
        //关闭热点
        ApMgr.disableAp(getContext());

        this.finish();
    }


    /**
     * 成功进入 文件接收列表UI 调用的finishNormal()
     */
    private void finishNormal() {
        if (mWifiAPBroadcastReceiver != null) {
            unregisterReceiver(mWifiAPBroadcastReceiver);
            mWifiAPBroadcastReceiver = null;
        }
        closeSocket();
        this.finish();
    }

    /**
     * 初始化
     */
    private void init() {
        radarLayout.setUseRing(true);
        radarLayout.setColor(getResources().getColor(R.color.white));
        radarLayout.setCount(4);
        radarLayout.start();
        //1.初始化热点
        WifiMgr.getInstance(getContext()).disableWifi();
        Log.e(TAG, "init: " + ApMgr.isApOn(getContext()));
        if (ApMgr.isApOn(getContext())) {
            ApMgr.disableAp(getContext());
        }

        mWifiAPBroadcastReceiver = new WifiAPBroadcastReceiver() {
            @Override
            public void onWifiApEnabled() {
                Log.i(TAG, "======>>>onWifiApEnabled !!!");
                if (!mIsInitialized) {
                    mUdpServerRuannable = createSendMsgToFileSenderRunnable();
                    AppContext.MAIN_EXECUTOR.execute(mUdpServerRuannable);
                    mIsInitialized = true;

                    tv_desc.setText(getResources().getString(R.string.tip_now_init_is_finish));
                    tv_desc.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tv_desc.setText(getResources().getString(R.string.tip_is_waitting_connect));
                        }
                    }, 2 * 1000);
                }
            }
        };
        IntentFilter filter = new IntentFilter(WifiAPBroadcastReceiver.ACTION_WIFI_AP_STATE_CHANGED);
        registerReceiver(mWifiAPBroadcastReceiver, filter);

        ApMgr.isApOn(getContext()); // check Ap state :boolean
        String ssid = TextUtils.isNullOrBlank(android.os.Build.DEVICE) ? Constant.DEFAULT_SSID : android.os.Build.DEVICE;
        ApMgr.configApState(getContext(), ssid); // change Ap state :boolean

        tv_device_name.setText(ssid);
        tv_desc.setText(getResources().getString(R.string.tip_now_is_initial));
    }

    @OnClick({R.id.tv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back: {
                onBackPressed();
                break;
            }
        }
    }

    /**
     * 创建发送UDP消息到 文件发送方 的服务线程
     */
    private Runnable createSendMsgToFileSenderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    startFileReceiverServer(Constant.DEFAULT_SERVER_COM_PORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    /**
     * 开启 文件接收方 通信服务 (必须在子线程执行)
     *
     * @param serverPort
     * @throws Exception
     */
    DatagramSocket mDatagramSocket;

    private void startFileReceiverServer(int serverPort) throws Exception {

        //网络连接上，无法获取IP的问题
        int count = 0;
        String localAddress = WifiMgr.getInstance(getContext()).getHotspotLocalIpAddress();
        while (localAddress.equals(Constant.DEFAULT_UNKOWN_IP) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(1000);
            localAddress = WifiMgr.getInstance(getContext()).getHotspotLocalIpAddress();
            Log.i(TAG, "receiver get local Ip ----->>>" + localAddress);
            count++;
        }

        mDatagramSocket = new DatagramSocket(serverPort);
        byte[] receiveData = new byte[1024];
        byte[] sendData = null;
        while (true) {
            //1.接收 文件发送方的消息
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            mDatagramSocket.receive(receivePacket);
            String msg = new String(receivePacket.getData()).trim();
            InetAddress inetAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
//            Log.i(TAG, "Get the msg from FileReceiver######>>>" + Constant.MSG_FILE_RECEIVER_INIT);
            if (msg != null && msg.startsWith(Constant.MSG_FILE_RECEIVER_INIT)) {
                Log.i(TAG, "Get the msg from FileReceiver######>>>" + Constant.MSG_FILE_RECEIVER_INIT);
                // 进入文件接收列表界面 (文件接收列表界面需要 通知 文件发送方发送 文件开始传输UDP通知)
                mHandler.obtainMessage(MSG_TO_FILE_RECEIVER_UI, new IpPortInfo(inetAddress, port)).sendToTarget();
            } else { //Receive and sender's file list
                if (msg != null) {
//                    FileInfo fileInfo = FileInfo.toObject(msg);
                    System.out.println("Get the FileInfo from FileReceiver######>>>" + msg);
                    parseFileInfo(msg);
                }
            }

            //2.反馈 文件发送方的消息
//            sendData = Constant.MSG_FILE_RECEIVER_INIT_SUCCESS.getBytes(BaseTransfer.UTF_8);
//            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, port);
//            serverSocket.send(sendPacket);
        }
    }

    /**
     * 解析FileInfo
     *
     * @param msg
     */
    private void parseFileInfo(String msg) {
        FileInfo fileInfo = FileInfo.toObject(msg);
        if (fileInfo != null && fileInfo.getFilePath() != null) {
            AppContext.getAppContext().addReceiverFileInfo(fileInfo);
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
