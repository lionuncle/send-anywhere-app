package com.estmob.android.sendanywhere.sdk.ui.example.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;



import com.estmob.android.sendanywhere.sdk.ui.example.AppContext;
import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.FileInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.FileUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ToastUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.ChooseFileActivity;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.adapter.FileInfoAdapter;
import com.estmob.android.sendanywhere.sdk.ui.example.utils.AnimationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Apk列表Fragment
 *
 * Created by mayubao on 2016/11/24.
 * Contact me 345269374@qq.com
 */
public class FileInfoFragment extends Fragment {

    @BindView(R.id.gv)
    GridView gv;
    @BindView(R.id.pb)
    ProgressBar pb;

    private int mType = FileInfo.TYPE_APK;
    private List<FileInfo> mFileInfoList = null;
    private FileInfoAdapter mFileInfoAdapter;

    @SuppressLint("ValidFragment")
    public FileInfoFragment(){
        super();
    }

    @SuppressLint("ValidFragment")
    public FileInfoFragment(int type) {
        super();
        this.mType = type;
    }

    public static FileInfoFragment newInstance(int type) {
        FileInfoFragment fragment = new FileInfoFragment(type);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_apk, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, rootView);

        if(mType == FileInfo.TYPE_APK){ //应用
            gv.setNumColumns(4);
        }else if(mType == FileInfo.TYPE_JPG){ //图片
            gv.setNumColumns(3);
            gv.setHorizontalSpacing(12);
            gv.setVerticalSpacing(12);
        }else if(mType == FileInfo.TYPE_MP3){ //音乐
            gv.setNumColumns(1);
        }else if(mType == FileInfo.TYPE_MP4){ //视频
            gv.setNumColumns(1);
        }

        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        init();//初始化界面

        return rootView;
    }

    private void init() {
        if(mType == FileInfo.TYPE_APK){
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_APK).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        }else if(mType == FileInfo.TYPE_JPG){
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_JPG).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        } else if (mType == FileInfo.TYPE_MP3) {
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_MP3).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        } else if (mType == FileInfo.TYPE_MP4) {
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_MP4).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        }

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileInfo fileInfo = mFileInfoList.get(position);
                if (AppContext.getAppContext().isExist(fileInfo)) {
                    AppContext.getAppContext().delFileInfo(fileInfo);
                    updateSelectedView();
                } else {
                    //1.添加任务
                    AppContext.getAppContext().addFileInfo(fileInfo);
                    //2.添加任务 动画
                    View startView = null;
                    View targetView = null;

                    startView = view.findViewById(R.id.iv_shortcut);
                    if (getActivity() != null && (getActivity() instanceof ChooseFileActivity)) {
                        ChooseFileActivity chooseFileActivity = (ChooseFileActivity) getActivity();
                        targetView = chooseFileActivity.getSelectedView();
                    }
                    AnimationUtils.setAddTaskAnimation(getActivity(), startView, targetView, null);
                }

                mFileInfoAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        updateFileInfoAdapter();
        super.onResume();
    }

    /**
     * 更新FileInfoAdapter
     */
    public void updateFileInfoAdapter(){
        if(mFileInfoAdapter != null){
            mFileInfoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新ChoooseActivity选中View
     */
    private void updateSelectedView(){
        if(getActivity() != null && (getActivity() instanceof ChooseFileActivity)){
            ChooseFileActivity chooseFileActivity = (ChooseFileActivity) getActivity();
            chooseFileActivity.getSelectedView();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 显示进度
     */
    public void showProgressBar(){
        if(pb != null) {
         pb.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏进度
     */
    public void hideProgressBar(){
        if(pb != null && pb.isShown()) {
            pb.setVisibility(View.GONE);
        }
    }


    /**
     * 获取ApkInfo列表任务
     */
    class GetFileInfoListTask extends AsyncTask<String, Integer, List<FileInfo>> {
        Context sContext = null;
        int sType = FileInfo.TYPE_APK;
        List<FileInfo> sFileInfoList = null;

        public GetFileInfoListTask(Context sContext, int type) {
            this.sContext = sContext;
            this.sType = type;
        }

        @Override
        protected void onPreExecute() {
            showProgressBar();
            super.onPreExecute();
        }



        @Override
        protected List doInBackground(String... params) {
            //FileUtils.getSpecificTypeFiles 只获取FileInfo的属性 filePath与size
            if(sType == FileInfo.TYPE_APK){
                sFileInfoList = getInstalledApps();

                //sFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{ FileInfo.EXTEND_APK});
                //Log.e("TAG", "doInBackground: "+ sFileInfoList );
                sFileInfoList = FileUtils.getDetailFileInfos(sContext, sFileInfoList, FileInfo.TYPE_APK);
            }else if(sType == FileInfo.TYPE_JPG){
                sFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{ FileInfo.EXTEND_JPG, FileInfo.EXTEND_JPEG,FileInfo.EXTEND_PNG});
                sFileInfoList = FileUtils.getDetailFileInfos(sContext, sFileInfoList, FileInfo.TYPE_JPG);
            }else if(sType == FileInfo.TYPE_MP3){
                sFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{ FileInfo.EXTEND_MP3});
                sFileInfoList = FileUtils.getDetailFileInfos(sContext, sFileInfoList, FileInfo.TYPE_MP3);
            }else if(sType == FileInfo.TYPE_MP4){
                sFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{ FileInfo.EXTEND_MP4});
                sFileInfoList = FileUtils.getDetailFileInfos(sContext, sFileInfoList, FileInfo.TYPE_MP4);
            }

            mFileInfoList = sFileInfoList;

            return sFileInfoList;
        }


        @Override
        protected void onPostExecute(List<FileInfo> list) {
            hideProgressBar();
            if(sFileInfoList != null && sFileInfoList.size() > 0){
                if(mType == FileInfo.TYPE_APK){ //应用
                    mFileInfoAdapter = new FileInfoAdapter(sContext, sFileInfoList, FileInfo.TYPE_APK);
                    gv.setAdapter(mFileInfoAdapter);
                }else if(mType == FileInfo.TYPE_JPG){ //图片
                    mFileInfoAdapter = new FileInfoAdapter(sContext, sFileInfoList, FileInfo.TYPE_JPG);
                    gv.setAdapter(mFileInfoAdapter);
                }else if(mType == FileInfo.TYPE_MP3){ //音乐
                    mFileInfoAdapter = new FileInfoAdapter(sContext, sFileInfoList, FileInfo.TYPE_MP3);
                    gv.setAdapter(mFileInfoAdapter);
                }else if(mType == FileInfo.TYPE_MP4){ //视频
                    mFileInfoAdapter = new FileInfoAdapter(sContext, sFileInfoList, FileInfo.TYPE_MP4);
                    gv.setAdapter(mFileInfoAdapter);
                }
            }else{
                ToastUtils.show(sContext, sContext.getResources().getString(R.string.tip_has_no_apk_info));
            }
        }
    }

    private List<FileInfo> getInstalledApps() {
       // PackageManager pm = getActivity().getPackageManager();
        List<FileInfo> apps = new ArrayList<FileInfo>();
        List<PackageInfo> packs = getActivity().getPackageManager().getInstalledPackages(0);
        //List<PackageInfo> packs = getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if ((!isSystemPackage(p))) {
                String appName = p.applicationInfo.loadLabel(getActivity().getPackageManager()).toString();
                String filepath = p.applicationInfo.publicSourceDir;
                Drawable icon = p.applicationInfo.loadIcon(getActivity().getPackageManager());
                File file = new File( p.applicationInfo.publicSourceDir);
                long size = file.length();
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilePath(filepath);
                fileInfo.setIcon(icon);
                fileInfo.setSize(size);
                fileInfo.setName(appName);
                apps.add(fileInfo);
               // apps.add(new FileInfo(appName, icon, packages));
            }
        }
        return apps;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
