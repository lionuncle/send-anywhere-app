package com.estmob.android.sendanywhere.sdk.ui.example.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import com.estmob.android.sendanywhere.sdk.ui.example.core.FileSender;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.FileInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.FileUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ToastUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.WifiMgr;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.adapter.FileSenderAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
public class FileSenderActivity extends BaseActivity {

    private static final String TAG = FileSenderActivity.class.getSimpleName();

    /**
     * Topbar相关UI
     */
    @BindView(R.id.tv_back)
    ImageView tv_back;
    @BindView(R.id.tv_title)
    TextView tv_title;

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
     * 其他UI
     */
    @BindView(R.id.lv_result)
    ListView lv_result;

    List<Map.Entry<String, FileInfo>> mFileInfoMapList;

    FileSenderAdapter mFileSenderAdapter;

    List<FileSender> mFileSenderList = new ArrayList<FileSender>();
//    ScanResult mScanResult;


    long mTotalLen = 0;     //所有总文件的进度
    long mCurOffset = 0;    //每次传送的偏移量
    long mLastUpdateLen = 0; //每个文件传送onProgress() 之前的进度
    String[] mStorageArray = null;


    long mTotalTime = 0;
    long mCurTimeOffset = 0;
    long mLastUpdateTime = 0;
    String[] mTimeArray = null;

    int mHasSendedFileCount = 0;


    public static final int MSG_UPDATE_FILE_INFO = 0X6666;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //TODO 未完成 handler实现细节以及封装
            if(msg.what == MSG_UPDATE_FILE_INFO){
                updateTotalProgressView();

                if(mFileSenderAdapter != null) mFileSenderAdapter.notifyDataSetChanged();
            }

        }
    };

    /**
     * 更新进度 和 耗时的 View
     */
    private void updateTotalProgressView() {
        try{
            //Set the total size of the transfer
            mStorageArray = FileUtils.getFileSizeArrayStr(mTotalLen);
            tv_value_storage.setText(mStorageArray[0]);
            tv_unit_storage.setText(mStorageArray[1]);

            //Set the time of transmission
            mTimeArray = FileUtils.getTimeByArrayStr(mTotalTime);
            tv_value_time.setText(mTimeArray[0]);
            tv_unit_time.setText(mTimeArray[1]);


            //Set the progress bar of the transfer
            if(mHasSendedFileCount == AppContext.getAppContext().getFileInfoMap().size()){
                pb_total.setProgress(100);
                txtSuccess.setVisibility(View.VISIBLE);
                tv_value_storage.setTextColor(getResources().getColor(R.color.black));
                tv_value_time.setTextColor(getResources().getColor(R.color.black));
                return;
            }

            long total = AppContext.getAppContext().getAllSendFileInfoSize();
            int percent = (int)(mTotalLen * 100 /  total);
            Log.e(TAG, "updateTotalProgressView: "+ percent );
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_sender);

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
//        super.onBackPressed();
        //需要判断是否有文件在发送？
        if(hasFileSending()){
            showExistDialog();
            return;
        }

        finishNormal();
    }

    /**
     * 正常退出
     */
    private void finishNormal(){
//        AppContext.FILE_SENDER_EXECUTOR.
        WifiMgr.getInstance(getContext()).disableWifi();
        stopAllFileSendingTask();
        AppContext.getAppContext().getFileInfoMap().clear();

        this.finish();
    }

    /**
     * 停止所有的文件发送任务
     */
    private void stopAllFileSendingTask(){
        for(FileSender fileSender : mFileSenderList){
            if(fileSender != null){
                fileSender.stop();
            }
        }
    }

    /**
     * 判断是否有文件在传送
     */
    private boolean hasFileSending(){
        for(FileSender fileSender : mFileSenderList){
            if(fileSender.isRunning()){
                return true;
            }
        }
        return false;
    }

    /**
     * 显示是否退出 对话框
     */
    private void showExistDialog(){
//        new AlertDialog.Builder(getContext())
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tip_now_has_task_is_running_exist_now))
                .setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishNormal();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.str_no), null)
                .create()
                .show();
    }

    /**
     * 初始化
     */
    private void init(){
//        mScanResult = getIntent().getParcelableExtra(Constant.KEY_SCAN_RESULT);

        tv_title.setVisibility(View.VISIBLE);
        tv_title.setText(getResources().getString(R.string.title_file_transfer));

        pb_total.setMax(100);

        mFileSenderAdapter = new FileSenderAdapter(getContext());
        lv_result.setAdapter(mFileSenderAdapter);


//        Map<String, FileInfo> dataMap = AppContext.getAppContext().getFileInfoMap();
//        List<FileInfo> fileInfoList = new ArrayList<>(dataMap.values());
//        Collections.sort(fileInfoList, Constant.DEFAULT_COMPARATOR2);

        List<Map.Entry<String, FileInfo>> fileInfoMapList = new ArrayList<Map.Entry<String, FileInfo>>(AppContext.getAppContext().getFileInfoMap().entrySet());
        mFileInfoMapList = fileInfoMapList;
        Collections.sort(fileInfoMapList, Constant.DEFAULT_COMPARATOR);


        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        //TODO
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_FILE);
        }else{
            initSendServer(fileInfoMapList);//开启传送文件
        }

//        AppContext.FILE_SENDER_EXECUTOR.execute();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_FILE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSendServer(mFileInfoMapList);//Turn on sending documents
            } else {
                // Permission Denied
                ToastUtils.show(this, getResources().getString(R.string.tip_permission_denied_and_not_send_file));
                finishNormal();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * 开始传送文件
     * @param fileInfoMapList
     */
    private void initSendServer(List<Map.Entry<String, FileInfo>> fileInfoMapList) {
        String serverIp = WifiMgr.getInstance(getContext()).getIpAddressFromHotspot();
        for(Map.Entry<String, FileInfo> entry : fileInfoMapList){
            final FileInfo fileInfo = entry.getValue();
            FileSender fileSender = new FileSender(getContext(), fileInfo, serverIp, Constant.DEFAULT_SERVER_PORT);
            fileSender.setOnSendListener(new FileSender.OnSendListener() {
                @Override
                public void onStart() {
                    mLastUpdateLen = 0;
                    mLastUpdateTime = System.currentTimeMillis();

                }

                @Override
                public void onProgress(long progress, long total) {
                    //TODO 更新
                    //=====Update progress flow time view start ====//
                    mCurOffset = progress - mLastUpdateLen > 0 ? progress - mLastUpdateLen : 0;
                    mTotalLen = mTotalLen + mCurOffset;
                    mLastUpdateLen = progress;

                    mCurTimeOffset = System.currentTimeMillis() - mLastUpdateTime > 0 ? System.currentTimeMillis() - mLastUpdateTime : 0;
                    mTotalTime = mTotalTime + mCurTimeOffset;
                    mLastUpdateTime = System.currentTimeMillis();
                    //=====Update progress flow time view end ====//

                    //Update file transfer progress ＵＩ
                    fileInfo.setProcceed(progress);
                    AppContext.getAppContext().updateFileInfo(fileInfo);
                    mHandler.sendEmptyMessage(MSG_UPDATE_FILE_INFO);
                }

                @Override
                public void onSuccess(FileInfo fileInfo) {
                    //=====Update progress flow time view start ====//
                    mHasSendedFileCount ++;

                    mTotalLen = mTotalLen + (fileInfo.getSize() - mLastUpdateLen);
                    mLastUpdateLen = 0;
                    mLastUpdateTime = System.currentTimeMillis();
                    //=====Update progress flow time view end ====//

                    System.out.println(Thread.currentThread().getName());
                    //TODO 成功
                    fileInfo.setResult(FileInfo.FLAG_SUCCESS);
                    AppContext.getAppContext().updateFileInfo(fileInfo);
                    mHandler.sendEmptyMessage(MSG_UPDATE_FILE_INFO);
                }

                @Override
                public void onFailure(Throwable t, FileInfo fileInfo) {
                    mHasSendedFileCount ++;//Statistics sent files
                    //TODO 失败
                    fileInfo.setResult(FileInfo.FLAG_FAILURE);
                    AppContext.getAppContext().updateFileInfo(fileInfo);
                    mHandler.sendEmptyMessage(MSG_UPDATE_FILE_INFO);
                }
            });

            mFileSenderList.add(fileSender);
            AppContext.FILE_SENDER_EXECUTOR.execute(fileSender);
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

}
