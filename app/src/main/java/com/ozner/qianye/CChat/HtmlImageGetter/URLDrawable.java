package com.ozner.qianye.CChat.HtmlImageGetter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;

import com.ozner.qianye.R;


/**
 * Created by xinde on 2015/12/25.
 */
public class URLDrawable extends BitmapDrawable {
    protected Drawable drawable;
    @SuppressWarnings("deprecation")
    public URLDrawable(Context context) {
        this.setBounds(getDefaultImageBounds(context));
        drawable = context.getResources().getDrawable(R.drawable.image_empty);
        int newwidth = 100;
        int newheight = 100;
        double factor = 1;
        double fx = (double) drawable.getIntrinsicWidth() / (double) newwidth;
        double fy = (double) drawable.getIntrinsicHeight() / (double) newheight;
        factor = fx > fy ? fx : fy;
        if (factor < 1) factor = 1;
        newwidth = (int) (drawable.getIntrinsicWidth() / factor);
        newheight = (int) (drawable.getIntrinsicHeight() / factor);

        drawable.setBounds(0,0,newwidth,newheight);
//        this.setBounds(0,0,400,300);
//        drawable = context.getResources().getDrawable(R.drawable.switch_on);
//        drawable.setBounds(0,0,400,300);
    }
    @Override
    public void draw(Canvas canvas) {
        if (drawable != null) {
            drawable.draw(canvas);
        }
    }

    @SuppressWarnings("deprecation")
    public Rect getDefaultImageBounds(Context context) {
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = (int) (width * 3 / 4);
        Rect bounds = new Rect(0, 0, width, height);
        return bounds;
    }
}
