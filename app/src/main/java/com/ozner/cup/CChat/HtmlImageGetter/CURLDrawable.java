package com.ozner.cup.CChat.HtmlImageGetter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by C-sir@hotmail.com  on 2015/12/29.
 */
public class CURLDrawable extends BitmapDrawable {
    protected Bitmap bitmap;
    @Override
    public void  draw(Canvas canvas)
    {
        if(bitmap!=null)
            canvas.drawBitmap(bitmap,0,0,getPaint());
    }
}
