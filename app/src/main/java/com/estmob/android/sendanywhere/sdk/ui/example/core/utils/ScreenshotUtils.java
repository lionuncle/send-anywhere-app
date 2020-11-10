package com.estmob.android.sendanywhere.sdk.ui.example.core.utils;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 缩略图工具类
 *
 * Created by mayubao on 2016/11/14.
 * Contact me 345269374@qq.com
 */
public class ScreenshotUtils {

    /**
     * 创建缩略图
     *
     * @param filePath
     * @return
     */
    public static Bitmap createVideoThumbnail(String filePath){
        Log.e("TAG", "handler: "+filePath);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MICRO_KIND);
        return bitmap;
    }


    /**
     * 将图片转换成指定宽高
     *
     * @param source
     * @param width
     * @param height
     * @return
     */
    public static Bitmap extractThumbnail(Bitmap source, int width, int height){
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(source, width, height);
        return bitmap;
    }


}
