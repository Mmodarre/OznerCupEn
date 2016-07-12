package com.ozner.cup.CChat.HtmlImageGetter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by C-sir@hotmail.com  on 2015/12/29.
 */
public class CURLImageParser implements Html.ImageGetter{
    TextView textView;
    public CURLImageParser(TextView tv)
    {
        textView=tv;
    }
    @Override
    public Drawable getDrawable(String source){
        final CURLDrawable urlDrawable = new CURLDrawable();
        Object s=textView.getTag();
        if(textView.getTag()==null) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true) // 加载图片时会在内存中加载缓存
                .cacheOnDisk(true) // 加载图片时会在磁盘中加载缓
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();
        ImageLoader.getInstance().loadImage(source, options,new SimpleImageLoadingListener() {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadimage) {
            urlDrawable.bitmap = loadimage;
            urlDrawable.setBounds(0, 0, loadimage.getWidth(), loadimage.getHeight());
            textView.invalidate();
            textView.setText(textView.getText().toString().replace("\\r\\n",""));
            textView.setTag(loadimage);//缓存
        }
    });
    }else{
    Bitmap bitmap=(Bitmap)textView.getTag();
    urlDrawable.bitmap = (Bitmap)textView.getTag();
    urlDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }
        return urlDrawable;
    }

}
