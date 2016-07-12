package com.ozner.cup.CChat.HtmlImageGetter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.view.Display;
import android.widget.TextView;
import java.net.URL;

/**
 * Created by xinde on 2015/12/25.
 */
public class URLImageGetter implements Html.ImageGetter {
    TextView textView;
    Context context;

    public URLImageGetter(Context contxt, TextView textView) {
        this.context = contxt;
        this.textView = textView;
    }

    @Override
    public Drawable getDrawable(String paramString) {
        URLDrawable urlDrawable = new URLDrawable(context);
//        ImageView imageView = new ImageView(context);
//        imageView.setLayoutParams(new LinearLayout.LayoutParams(400, 300));
//        URLDrawable ivDrawable = imageView.getDrawable();
        ImageGetterAsyncTask getterTask = new ImageGetterAsyncTask(urlDrawable);
        getterTask.execute(paramString);
        return urlDrawable;
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable drawable) {
            this.urlDrawable = drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {

                int newwidth = 600;
                int newheight = 600;
                double factor = 1;
                double fx = (double) result.getIntrinsicWidth() / (double) newwidth;
                double fy = (double) result.getIntrinsicHeight() / (double) newheight;
                factor = fx > fy ? fx : fy;
                if (factor < 1) factor = 1;
                newwidth = (int) (result.getIntrinsicWidth() / factor);
                newheight = (int) (result.getIntrinsicHeight() / factor);
                result.setBounds(0, 0, newwidth, newheight);
                urlDrawable.drawable = result;
                textView.setWidth(newwidth);
                textView.setHeight(newheight);
                textView.setPadding(0,0,0,0);
              //  URLImageGetter.this.textView.requestLayout();
            }
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        public Drawable fetchDrawable(String url) {
            Drawable drawable = null;
            URL Url;
            try {
                Url = new URL(url);
                drawable = Drawable.createFromStream(Url.openStream(), "");
            } catch (Exception e) {
                return null;
            }
//按比例缩放图片
            Rect bounds = getDefaultImageBounds(context);
            int newwidth = bounds.width();
            int newheight = bounds.height();
//            int newwidth = 300;
//            int newheight = 300;
            double factor = 1;
            double fx = (double) drawable.getIntrinsicWidth() / (double) newwidth;
            double fy = (double) drawable.getIntrinsicHeight() / (double) newheight;
            factor = fx > fy ? fx : fy;
            if (factor < 1) factor = 1;
            newwidth = (int) (drawable.getIntrinsicWidth() / factor);
            newheight = (int) (drawable.getIntrinsicHeight() / factor);
            drawable.setBounds(0, 0, newwidth, newheight);
            return drawable;
        }
    }

    //预定图片宽高比例为 4:3
    @SuppressWarnings("deprecation")
    public Rect getDefaultImageBounds(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = (int) (width * 3 / 4);
        Rect bounds = new Rect(0, 0, width, height);
        return bounds;
    }
}
