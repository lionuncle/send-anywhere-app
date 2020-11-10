package com.estmob.android.sendanywhere.sdk.ui.example.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.estmob.android.sendanywhere.sdk.ui.example.AppContext;
import com.estmob.android.sendanywhere.sdk.ui.example.R;
import com.estmob.android.sendanywhere.sdk.ui.example.common.CommonAdapter;
import com.estmob.android.sendanywhere.sdk.ui.example.core.entity.FileInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ApkInfo Adapter
 *
 * Created by mayubao on 2016/11/24.
 * Contact me 345269374@qq.com
 */
public class FileInfoAdapter extends CommonAdapter<FileInfo> {

    /**
     * 文件类型的标识
     */
    private int mType = FileInfo.TYPE_APK;

    public FileInfoAdapter(Context context, List<FileInfo> dataList) {
        super(context, dataList);
    }

    public FileInfoAdapter(Context context, List<FileInfo> dataList, int type) {
        super(context, dataList);
        this.mType = type;
    }

    @Override
    public View convertView(int position, View convertView) {
        FileInfo fileInfo = getDataList().get(position);

        if(mType == FileInfo.TYPE_APK){ //APK convertView
            ApkViewHolder viewHolder = null;
            if(convertView == null){
                convertView = View.inflate(getContext(), R.layout.item_apk, null);
                viewHolder = new ApkViewHolder();
                viewHolder.iv_shortcut = (ImageView) convertView.findViewById(R.id.iv_shortcut);
                viewHolder.iv_ok_tick = (ImageView) convertView.findViewById(R.id.iv_ok_tick);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ApkViewHolder) convertView.getTag();
            }

            if(getDataList() != null && getDataList().get(position) != null){

                Collections.sort(getDataList(), new Comparator<FileInfo>() {
                    @Override
                    public int compare(final FileInfo object1, final FileInfo object2) {
                        return object1.getName().compareTo(object2.getName());
                    }
                });

                viewHolder.iv_shortcut.setImageBitmap(fileInfo.getBitmap());
                viewHolder.tv_name.setText(fileInfo.getName() == null ? "" : fileInfo.getName());
                viewHolder.tv_size.setText(fileInfo.getSizeDesc() == null ? "" : fileInfo.getSizeDesc());

                //全局变量是否存在FileInfo
                if(AppContext.getAppContext().isExist(fileInfo)){
                    viewHolder.iv_ok_tick.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.iv_ok_tick.setVisibility(View.GONE);
                }
            }
        }
        else if(mType == FileInfo.TYPE_JPG){ //JPG convertView
            JpgViewHolder viewHolder = null;
            if(convertView == null){
                convertView = View.inflate(getContext(), R.layout.item_jpg, null);
                viewHolder = new JpgViewHolder();
                viewHolder.iv_ok_tick = (ImageView) convertView.findViewById(R.id.iv_ok_tick);
                viewHolder.iv_shortcut = (ImageView) convertView.findViewById(R.id.iv_shortcut);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (JpgViewHolder) convertView.getTag();
            }

            if(getDataList() != null && getDataList().get(position) != null){

//                viewHolder.iv_shortcut.setImageBitmap(fileInfo.getBitmap());
                Glide
                    .with(getContext())
                    .load(fileInfo.getFilePath())
                    .centerCrop()
                    .placeholder(R.mipmap.icon_jpg)
                    .crossFade()
                    .into(viewHolder.iv_shortcut);

                //全局变量是否存在FileInfo
                if(AppContext.getAppContext().isExist(fileInfo)){
                    viewHolder.iv_ok_tick.setImageResource(R.drawable.ic_checkbox);
                }else{
                    viewHolder.iv_ok_tick.setImageResource(R.drawable.ic_uncheckbox);
                }
            }
        }
        else if(mType == FileInfo.TYPE_MP3){ //MP3 convertView
            Mp3ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = View.inflate(getContext(), R.layout.item_mp3, null);
                viewHolder = new Mp3ViewHolder();
                viewHolder.iv_ok_tick = (ImageView) convertView.findViewById(R.id.iv_ok_tick);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (Mp3ViewHolder) convertView.getTag();
            }

            if(getDataList() != null && getDataList().get(position) != null){

                Collections.sort(getDataList(), new Comparator<FileInfo>() {
                    @Override
                    public int compare(final FileInfo object1, final FileInfo object2) {
                        return object1.getName().compareTo(object2.getName());
                    }
                });

                viewHolder.tv_name.setText(fileInfo.getName() == null ? "" : fileInfo.getName());
                viewHolder.tv_size.setText(fileInfo.getSizeDesc() == null ? "" : fileInfo.getSizeDesc());

                //全局变量是否存在FileInfo
                if(AppContext.getAppContext().isExist(fileInfo)){
                    viewHolder.iv_ok_tick.setImageResource(R.drawable.ic_checkbox);
                }else{
                    viewHolder.iv_ok_tick.setImageResource(R.drawable.ic_uncheckbox);
                }
            }
        }
        else if(mType == FileInfo.TYPE_MP4){ //MP4 convertView
            Mp4ViewHolder viewHolder = null;
            if(convertView == null){
                convertView = View.inflate(getContext(), R.layout.item_mp4, null);
                viewHolder = new Mp4ViewHolder();
                viewHolder.iv_shortcut = (ImageView) convertView.findViewById(R.id.iv_shortcut);
                viewHolder.iv_ok_tick = (ImageView) convertView.findViewById(R.id.iv_ok_tick);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (Mp4ViewHolder) convertView.getTag();
            }

            if(getDataList() != null && getDataList().get(position) != null){
                //viewHolder.iv_shortcut.setImageBitmap(fileInfo.getBitmap());
                Collections.sort(getDataList(), new Comparator<FileInfo>() {
                    @Override
                    public int compare(final FileInfo object1, final FileInfo object2) {
                        return object1.getName().compareTo(object2.getName());
                    }
                });
                viewHolder.tv_name.setText(fileInfo.getName() == null ? "" : fileInfo.getName());
                viewHolder.tv_size.setText(fileInfo.getSizeDesc() == null ? "" : fileInfo.getSizeDesc());

                //全局变量是否存在FileInfo
                if(AppContext.getAppContext().isExist(fileInfo)){
                    viewHolder.iv_ok_tick.setImageResource(R.drawable.ic_checkbox);
                }else{
                    viewHolder.iv_ok_tick.setImageResource(R.drawable.ic_uncheckbox);
                }
            }
        }

        return convertView;
    }

    static class ApkViewHolder {
        ImageView iv_shortcut;
        ImageView iv_ok_tick;
        TextView tv_name;
        TextView tv_size;
    }

    static class JpgViewHolder {
        ImageView iv_shortcut;
        ImageView iv_ok_tick;
    }

    static class Mp3ViewHolder {
        ImageView iv_ok_tick;
        TextView tv_name;
        TextView tv_size;
    }

    static class Mp4ViewHolder {
        ImageView iv_shortcut;
        ImageView iv_ok_tick;
        TextView tv_name;
        TextView tv_size;
    }
}
