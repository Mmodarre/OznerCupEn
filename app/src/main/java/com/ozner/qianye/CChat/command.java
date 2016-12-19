package com.ozner.qianye.CChat;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ozner.qianye.CChat.gif.AnimatedGifDrawable;
import com.ozner.qianye.CChat.gif.AnimatedImageSpan;
import com.ozner.qianye.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by C-sir@hotmail.com  on 2015/12/30.
 */
public class command {
    /**
     * 图片加载监听事件
     **/
    public static class HtmlImage {
        public static Bitmap scalBitmap(Bitmap src, int dstW, int dstH) {
            if (src != null) {
                int len = src.getByteCount();
                if (len < 100 * 1024) {
                    return src;
                }

                float scaleX = (float) dstW / src.getWidth();
                float scaleY = (float) dstH / src.getHeight();
                float scale = scaleX < scaleY ? scaleX : scaleY;
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
//            Bitmap newbmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
                Bitmap bmp = Bitmap.createScaledBitmap(src, (int) (src.getWidth() * scale), (int) (src.getHeight() * scale), false);
                return bmp;
            } else {
                return null;
            }
        }

        private static List<String> ImagePath(String msg) {
//            Log.e("tag", "cchat_msg:" + msg);
            List<String> path = new ArrayList<String>();
            Pattern p = Pattern.compile("<img.*?src=\\\"(.*).*\\\"");
            Matcher m = p.matcher(msg);
            if (m != null) {
                while (m.find()) {
                    String imagepath = m.group(1);
//                    Log.e("tag", "imagePath_Pre:" + imagepath);
                    int start = imagepath.indexOf("\"");
                    if (start >= 0)
                        imagepath = imagepath.substring(0, start);
//                    Log.e("tag", "imagePath_After:" + imagepath);
                    path.add(imagepath);
                }
            }
            return path;
        }

        public static int ImageFormInternet(String msg) {
            int i = 0;
            Pattern p = Pattern.compile("<img.*?src=\\\"(.*).*\\\"");
            Matcher m = p.matcher(msg);
            if (m != null) {
                while (m.find()) {
                    String imagepath = m.group(1);
//                    Log.e("tag", "imagepath----" + imagepath);
                    if (imagepath.toLowerCase().contains("http")) {
                        if (imagepath.toLowerCase().contains(".gif")) {
                            return 2;//gif图片
                        }
                        //网络图片
                        return 1;
                    } else {


                        //本地图片
                        return 0;
                    }
                }
            }
            return -1;
        }

        public static void TextViewDrawBitmap(TextView tv, String msg) {
            class AnimateFirstDisplayListener extends
                    SimpleImageLoadingListener {
                private TextView tv;

                public AnimateFirstDisplayListener(TextView tv) {
                    this.tv = tv;
                }

                final List<String> displayedImages = Collections
                        .synchronizedList(new LinkedList<String>());

                @Override
                public void onLoadingStarted(String imageUri, View view) {
//                    Log.e("CSIRCHAT", "netImg:开始加载网络图片" + imageUri);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                    Log.e("CSIRCHAT", "netImg:加载网络图片失败" + imageUri + failReason.toString());
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
//                    Log.e("CSIRCHAT", "netImg:加载网络图片返回");
                }

                @Override
                public void onLoadingComplete(String imageUri, View view,
                                              Bitmap loadedImage) {
//                    Log.e("CSIRCHAT", "netImg:加载到网络图片");
                    if (loadedImage != null) {
//                        Log.e("CSIRCHAT", "netImg:加载到网络图片");
                        boolean firstDisplay = !displayedImages.contains(imageUri);
                        if (firstDisplay) {
                            Bitmap newbmp = scalBitmap(loadedImage, 800, 600);
                            ImageSpan imgSpan = new ImageSpan(tv.getContext(), newbmp);
                            SpannableString spanString = new SpannableString("i");
                            spanString.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv.setText(spanString);

                        }
                    } else {
                        tv.setText(imageUri);
                    }
                    tv.invalidate();
                }
            }

            List<String> path = ImagePath(msg);
            int netstate = ImageFormInternet(msg);
            if (netstate == 1)//网络图片
            {
                AnimateFirstDisplayListener animateFirstListener = new AnimateFirstDisplayListener(tv);
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true) // 加载图片时会在内存中加载缓存
                        .cacheOnDisk(true) // 加载图片时会在磁盘中加载缓
                        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                        .build();
                Bitmap bitmap = BitmapFactory.decodeResource(tv.getResources(), R.drawable.image_loading_new);
//                Log.e("CSIRCHAT", "netImg:网络图片" + path.get(0));
                ImageSpan imgSpan = new ImageSpan(tv.getContext(), bitmap);
                SpannableString spanString = new SpannableString("i");
                spanString.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(spanString);
                ImageLoader.getInstance().loadImage(path.get(0), options, animateFirstListener);
            } else if (netstate == 0)    //本地图片
            {
//                Log.e("tag", "local_path:" + path.get(0));
                Bitmap bitmap = BitmapFactory.decodeFile(path.get(0));

                Bitmap newbmp = scalBitmap(bitmap, 800, 600);
                ImageSpan imgSpan = new ImageSpan(tv.getContext(), newbmp);

                SpannableString spanString = new SpannableString("i");
                spanString.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(spanString);
//                if (!bitmap.isRecycled())
//                    bitmap.recycle();
//                if (!newbmp.isRecycled())
//                    newbmp.recycle();
            } else if (netstate == 2) {
                String msgTemp = msg;
//                Pattern p = Pattern.compile("<img.*?src=\\\".*?\\\".*? .*?>");
                Pattern p = Pattern.compile("<img.*?src=\\\".*?>");
                Matcher m = p.matcher(msg);
                if (m != null) {
                    while (m.find()) {
                        try {
                            String imgtag = m.group();
//                        Log.e("match", "imgtag:" + imgtag);
                            Pattern patSrc = Pattern.compile("http:.*?\\.gif");
                            Matcher mSrc = patSrc.matcher(imgtag);
                            if (mSrc != null && mSrc.find()) {
                                String matchSrc = mSrc.group();
//                            Log.e("match", "matchSrc:" + matchSrc);
                                int start = matchSrc.lastIndexOf("/") + 1;
                                int end = matchSrc.indexOf(".gif");
                                int index = Integer.parseInt(matchSrc.substring(start, end));
                                String num = "";
                                if (index < 10) {
                                    num += "0";
                                }
                                if (index < 100) {
                                    num += "0";
                                }
                                num += String.valueOf(index);
                                String resStr = "#[face/png/f_static_" + num + ".png]#";
//                            Log.e("match", "index:" + index + " , " + resStr);
                                msgTemp = msgTemp.replace(imgtag, resStr);
                            } else {
                                Log.e("match", "mSrc is null or not find");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();

                        }
                    }
                } else {
                    Log.e("match", "m is null");
                }
                SpannableStringBuilder sb = dealgifMsg(tv,
                        msgTemp);
                tv.setText(sb);
            }
        }

        private static SpannableStringBuilder dealgifMsg(final TextView gifTextView, String content) {
            SpannableStringBuilder sb = new SpannableStringBuilder(content);
            String regex = "(\\#\\[face/png/f_static_)\\d{3}(.png\\]\\#)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(content);
            while (m.find()) {
                String tempText = m.group();
                try {
                    String num = tempText.substring("#[face/png/f_static_".length(), tempText.length() - ".png]#".length());
                    String gif = "face/gif/f" + num + ".gif";
                    /**
                     * 如果open这里不抛异常说明存在gif，则显示对应的gif
                     * 否则说明gif找不到，则显示png
                     * */
                    InputStream is = gifTextView.getContext().getAssets().open(gif);
                    sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is, new AnimatedGifDrawable.UpdateListener() {
                                @Override
                                public void update() {
                                    gifTextView.postInvalidate();
                                }
                            })), m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    is.close();
                } catch (Exception e) {
                    String png = tempText.substring("#[".length(), tempText.length() - "]#".length());
                    try {
                        sb.setSpan(new ImageSpan(gifTextView.getContext(), BitmapFactory.decodeStream(gifTextView.getContext().getAssets().open(png))), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }

            return sb;
        }
    }


}
