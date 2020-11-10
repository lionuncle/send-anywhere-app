package com.estmob.android.sendanywhere.sdk.ui.example.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.estmob.android.sendanywhere.sdk.ui.example.AppContext;
import com.estmob.android.sendanywhere.sdk.ui.example.Constant;
import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.common.BaseActivity;
import com.estmob.android.sendanywhere.sdk.ui.example.core.BaseTransfer;
import com.estmob.android.sendanywhere.sdk.ui.example.core.FileReceiver;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.FileInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.IpPortInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ApMgr;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.FileUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ToastUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.WifiMgr;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.adapter.FileReceiverAdapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 文件接收列表界面
 *
 * ReceiverWaitingActivity --->>> 文件接收列表界面
 *
 * 前提条件：
 * 1.文件发送方连接上文件接收方的局域网络(即为文件接收方的热点) 【TODO: 如何 文件接收方 收到 文件发送方的连接信息？ UDP？】
 *      如果是在文件发送UDP的话，那么应该在ReceiverWaitingActivity里面去监听
 * 2.
 *
 * Created by mayubao on 2016/11/28.
 * Contact me 345269374@qq.com
 */
public class FileReceiverActivity extends BaseActivity {

    private static final String TAG = FileReceiverActivity.class.getSimpleName();

    /**
     * Topbar相关UI
     */
    @BindView(R.id.tv_back)
    ImageView tv_back;
    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.tv_unit_has_send)
    TextView txt_send;
    @BindView(R.id.txt_success)
    TextView txtSuccess;
    /**
     * 进度条 已传 耗时等UI组件
     */
    @BindView(R.id.pb_total)
    ProgressBar pb_total;
    @BindView(R.id.tv_value_storage)
    TextView tv_value_storage;
    @BindView(R.id.tv_unit_storage)
    TextView tv_unit_storage;
    @BindView(R.id.tv_value_time)
    TextView tv_value_time;
    @BindView(R.id.tv_unit_time)
    TextView tv_unit_time;


    /**
     * 扫描结果
     */
    @BindView(R.id.lv_result)
    ListView lv_result;

    FileReceiverAdapter mFileReceiverAdapter;
    FileInfo mCurFileInfo;

    IpPortInfo mIpPortInfo;

    ServerRunnable mReceiverServer;


    long mTotalLen = 0;     //所有总文件的进度
    long mCurOffset = 0;    //每次传送的偏移量
    long mLastUpdateLen = 0; //每个文件传送onProgress() 之前的进度
    String[] mStorageArray = null;


    long mTotalTime = 0;
    long mCurTimeOffset = 0;
    long mLastUpdateTime = 0;
    String[] mTimeArray = null;

    int mHasSendedFileCount = 0;

    public static final int MSG_FILE_RECEIVER_INIT_SUCCESS = 0X4444;
    public static final int MSG_ADD_FILE_INFO = 0X5555;
    public static final int MSG_UPDATE_FILE_INFO = 0X6666;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_FILE_RECEIVER_INIT_SUCCESS){
                sendMsgToFileSender(mIpPortInfo);
            }else if(msg.what == MSG_ADD_FILE_INFO){
                //ADD FileInfo 到 Adapter
                FileInfo fileInfo = (FileInfo) msg.obj;
                //ToastUtils.show(getContext(), "Receive a task:" + (fileInfo != null ? fileInfo.getFilePath() : ""));
            }else if(msg.what == MSG_UPDATE_FILE_INFO){
                //ADD FileInfo 到 Adapter
                updateTotalProgressView();
                if(mFileReceiverAdapter != null) mFileReceiverAdapter.update();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_receiver);

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
        //关闭TCP UDP 资源
        //清除选中文件的信息
        //关闭热点

        if(mReceiverServer != null){
            mReceiverServer.close();
            mReceiverServer = null;
        }

        closeSocket();

        AppContext.getAppContext().getReceiverFileInfoMap().clear();

        ApMgr.disableAp(getContext());
        this.finish();
    }

    /**
     * 初始化
     */
    private void init(){
        //界面初始化
        tv_title.setVisibility(View.VISIBLE);
        tv_title.setText(getResources().getString(R.string.title_file_transfer));
        txt_send.setText("Received");

        mFileReceiverAdapter = new FileReceiverAdapter(getContext());
        lv_result.setAdapter(mFileReceiverAdapter);

        mIpPortInfo = (IpPortInfo) getIntent().getSerializableExtra(Constant.KEY_IP_PORT_INFO);


        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        //TODO
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_FILE);
        }else{
            initServer(); //启动接收服务
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_FILE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initServer(); //启动接收服务
            } else {
                // Permission Denied
                ToastUtils.show(this, getResources().getString(R.string.tip_permission_denied_and_not_receive_file));
                onBackPressed();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * 开启文件接收端服务
     */
    private void initServer() {
        mReceiverServer = new ServerRunnable(Constant.DEFAULT_SERVER_PORT);
        new Thread(mReceiverServer).start();
    }

    /**
     * 更新进度 和 耗时的 View
     */
    private void updateTotalProgressView() {
        try{
            //设置传送的总容量大小
            mStorageArray = FileUtils.getFileSizeArrayStr(mTotalLen);
            tv_value_storage.setText(mStorageArray[0]);
            tv_unit_storage.setText(mStorageArray[1]);

            //设置传送的时间情况
            mTimeArray = FileUtils.getTimeByArrayStr(mTotalTime);
            tv_value_time.setText(mTimeArray[0]);
            tv_unit_time.setText(mTimeArray[1]);


            //设置传送的进度条情况
            if(mHasSendedFileCount == AppContext.getAppContext().getReceiverFileInfoMap().size()){
                //pb_total.setProgress(0);
                txtSuccess.setVisibility(View.VISIBLE);
                tv_value_storage.setTextColor(getResources().getColor(R.color.black));
                tv_value_time.setTextColor(getResources().getColor(R.color.black));
                return;
            }

            long total = AppContext.getAppContext().getAllReceiverFileInfoSize();
            int percent = (int)(mTotalLen * 100 /  total);
            pb_total.setProgress(percent);

            if(total  == mTotalLen){
                pb_total.setProgress(0);
                tv_value_storage.setTextColor(getResources().getColor(R.color.black));
                tv_value_time.setTextColor(getResources().getColor(R.color.black));
            }
        }catch (Exception e){
            //convert storage array has some problem
        }
    }

    @OnClick({R.id.tv_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_back:{
                onBackPressed();
                break;
            }
        }
    }

    public void sendMsgToFileSender(final IpPortInfo ipPortInfo){
        new Thread(){
            @Override
            public void run() {
                try {
                    sendFileReceiverInitSuccessMsgToFileSender(ipPortInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 通知文件发送方 ===>>> 文件接收方初始化完毕
     */

    DatagramSocket mDatagramSocket;
    public void sendFileReceiverInitSuccessMsgToFileSender(IpPortInfo ipPortInfo) throws Exception {
        Log.i(TAG, "sendFileReceiverInitSuccessMsgToFileSender------>>>start");
        mDatagramSocket = new DatagramSocket(ipPortInfo.getPort() +1);
        byte[] receiveData = new byte[1024];
        byte[] sendData = null;
        InetAddress ipAddress = ipPortInfo.getInetAddress();
        //1.发送 文件接收方 初始化
        sendData = Constant.MSG_FILE_RECEIVER_INIT_SUCCESS.getBytes(BaseTransfer.UTF_8);
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, ipAddress, ipPortInfo.getPort());
        mDatagramSocket.send(sendPacket);
        Log.i(TAG, "Send Msg To FileSender######>>>" + Constant.MSG_FILE_RECEIVER_INIT_SUCCESS);
        Log.i(TAG, "sendFileReceiverInitSuccessMsgToFileSender------>>>end");
    }

    /**
     * 关闭UDP Socket 流
     */
    private void closeSocket(){
        if(mDatagramSocket != null){
            if(mDatagramSocket.isConnected()) {
                mDatagramSocket.disconnect();
            }
            if(!mDatagramSocket.isClosed()) {
                mDatagramSocket.close();
            }
            mDatagramSocket = null;
        }
    }


    /**
     * ServerSocket启动线程
     */
    class ServerRunnable implements Runnable {
        ServerSocket serverSocket;
        private int port;


        public ServerRunnable(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            Log.i(TAG, "------>>>Socket已经开启");
            try {
                serverSocket = new ServerSocket(Constant.DEFAULT_SERVER_PORT);
                mHandler.obtainMessage(MSG_FILE_RECEIVER_INIT_SUCCESS).sendToTarget();
                while (!Thread.currentThread().isInterrupted()){
                    Socket socket = serverSocket.accept();

                    //生成缩略图
                    FileReceiver fileReceiver = new FileReceiver(FileReceiverActivity.this,socket);
                    fileReceiver.setOnReceiveListener(new FileReceiver.OnReceiveListener() {
                        @Override
                        public void onStart() {
//                            handler.obtainMessage(MSG_SHOW_PROGRESS).sendToTarget();
                            mLastUpdateLen = 0;
                            mLastUpdateTime = System.currentTimeMillis();
                        }

                        @Override
                        public void onGetFileInfo(FileInfo fileInfo) {
                            mHandler.obtainMessage(MSG_ADD_FILE_INFO, fileInfo).sendToTarget();
                            mCurFileInfo = fileInfo;
                            AppContext.getAppContext().addReceiverFileInfo(mCurFileInfo);
                            mHandler.sendEmptyMessage(MSG_UPDATE_FILE_INFO);
                        }

                        @Override
                        public void onGetScreenshot(Bitmap bitmap) {
//                            handler.obtainMessage(MSG_SHOW_PROGRESS, bitmap).sendToTarget();
                        }

                        @Override
                        public void onProgress(long progress, long total) {
                            //=====更新进度 流量 时间视图 start ====//
                            mCurOffset = progress - mLastUpdateLen > 0 ? progress - mLastUpdateLen : 0;
                            mTotalLen = mTotalLen + mCurOffset;
                            mLastUpdateLen = progress;

                            mCurTimeOffset = System.currentTimeMillis() - mLastUpdateTime > 0 ? System.currentTimeMillis() - mLastUpdateTime : 0;
                            mTotalTime = mTotalTime + mCurTimeOffset;
                            mLastUpdateTime = System.currentTimeMillis();
                            //=====更新进度 流量 时间视图 end ====//

                            mCurFileInfo.setProcceed(progress);
                            AppContext.getAppContext().updateReceiverFileInfo(mCurFileInfo);
                            mHandler.sendEmptyMessage(MSG_UPDATE_FILE_INFO);
                        }

                        @Override
                        public void onSuccess(FileInfo fileInfo) {
                            //=====更新进度 流量 时间视图 start ====//
                            mHasSendedFileCount ++;

                            mTotalLen = mTotalLen + (fileInfo.getSize() - mLastUpdateLen);
                            mLastUpdateLen = 0;
                            mLastUpdateTime = System.currentTimeMillis();
                            //=====更新进度 流量 时间视图 end ====//

                            fileInfo.setResult(FileInfo.FLAG_SUCCESS);
                            AppContext.getAppContext().updateReceiverFileInfo(fileInfo);
                            mHandler.sendEmptyMessage(MSG_UPDATE_FILE_INFO);
                        }

                        @Override
                        public void onFailure(Throwable t, FileInfo fileInfo) {
                            mHasSendedFileCount ++;//统计发送文件

                            fileInfo.setResult(FileInfo.FLAG_FAILURE);
                            AppContext.getAppContext().updateFileInfo(fileInfo);
                            mHandler.sendEmptyMessage(MSG_UPDATE_FILE_INFO);
                        }
                    });

//                    mFileReceiver = fileReceiver;
//                    new Thread(fileReceiver).start();
                    AppContext.getAppContext().MAIN_EXECUTOR.execute(fileReceiver);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        /**
         * 关闭Socket 通信 (避免端口占用)
         */
        public void close(){
            if(serverSocket != null){
                try {
                    serverSocket.close();
                    serverSocket = null;
                } catch (IOException e) {
                }
            }
        }
    }
}
