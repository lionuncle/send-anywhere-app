package com.estmob.android.sendanywhere.sdk.ui.example.ui.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.estmob.android.sendanywhere.sdk.ui.example.AppContext;
import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.FileInfo;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.FileUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.core.utils.ToastUtils;
import com.estmob.android.sendanywhere.sdk.ui.example.ui.adapter.HistoryAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Apk列表Fragment
 * <p>
 * Created by mayubao on 2016/11/24.
 * Contact me 345269374@qq.com
 */
public class HistoryAudioFragment extends Fragment {

    @BindView(R.id.gv)
    GridView gv;
    @BindView(R.id.pb)
    ProgressBar pb;

    private HistoryAdapter mHistoryAdapter;
    private List<FileInfo> mHistoryList;
    @BindView(R.id.no_internet_layout)
    RelativeLayout no_internet_layout;
    /*@SuppressLint("ValidFragment")
    public HistoryFragment() {
        super();
    }
*/
   /* @SuppressLint("ValidFragment")
    public HistoryFragment(int type, int isSendRcv) {
        super();
        this.mType = type;
        this.mIsSendRcv = isSendRcv;
    }*/

    public static HistoryAudioFragment newInstance() {
        HistoryAudioFragment fragment = new HistoryAudioFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, rootView);
        gv.setNumColumns(1);

        init();//初始化界面

        return rootView;
    }

    private void init() {

        new GetFileInfoListTask(getContext()).executeOnExecutor(AppContext.MAIN_EXECUTOR);


        /*gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 更新FileInfoAdapter
     */
    /*public void updateFileInfoAdapter(){
        if(mFileInfoAdapter != null){
            mFileInfoAdapter.notifyDataSetChanged();
        }
    }*/

    /**
     * 更新ChoooseActivity选中View
     */

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
    public void showProgressBar() {
        if (pb != null) {
            pb.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏进度
     */
    public void hideProgressBar() {
        if (pb != null && pb.isShown()) {
            pb.setVisibility(View.GONE);
        }
    }


    /**
     * 获取ApkInfo列表任务
     */
    class GetFileInfoListTask extends AsyncTask<String, Integer, List<FileInfo>> {
        Context sContext = null;
        List<FileInfo> sHistoryList = null;

        public GetFileInfoListTask(Context sContext) {
            this.sContext = sContext;
        }

        @Override
        protected void onPreExecute() {
            showProgressBar();
            super.onPreExecute();
        }

        @Override
        protected List doInBackground(String... params) {
            //FileUtils.getSpecificTypeFiles 只获取FileInfo的属性 filePath与size
            sHistoryList = getMp3History();
            sHistoryList = FileUtils.getHistoryDetailFileInfos(sContext, sHistoryList, FileInfo.TYPE_MP3);
            mHistoryList = sHistoryList;
            return sHistoryList;
        }


        @Override
        protected void onPostExecute(List<FileInfo> list) {
            hideProgressBar();
            if (sHistoryList != null && sHistoryList.size() > 0) {
                mHistoryAdapter = new HistoryAdapter(sContext, sHistoryList, FileInfo.TYPE_MP3);
                gv.setAdapter(mHistoryAdapter);
            } else {
                ToastUtils.show(sContext, sContext.getResources().getString(R.string.tip_has_no_apk_info));
            }
        }
    }

    public List<FileInfo> getMp3History() {
        List<FileInfo> listApk = new ArrayList<>();
        String path = FileUtils.getSpecifyDirPath(FileInfo.TYPE_MP3);
        //Log.d("Files", "Path: " + path);
        File directory = new File(path);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            // Log.d("Files", "Size: " + files.length);
            if (files.length <= 0) {
                Toast.makeText(getActivity(), "History Data Not Available", Toast.LENGTH_SHORT).show();
            } else {
                for (File file : files) {
                    //Log.e("Files", "FileName:" + file.getName());
                    FileInfo fileInfo = new FileInfo();
                    String filepah = file.getPath();
                    String filename = file.getName();
                    long size = file.length();
                    fileInfo.setName(filename);
                    fileInfo.setFilePath(filepah);
                    fileInfo.setSize(size);
                    listApk.add(fileInfo);
                }
            }
        } else {
            no_internet_layout.setVisibility(View.VISIBLE);
            //Toast.makeText(getActivity(), "Directory Not Available", Toast.LENGTH_SHORT).show();
        }
        return listApk;
    }
}

