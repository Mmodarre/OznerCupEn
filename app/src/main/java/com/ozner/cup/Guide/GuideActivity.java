package com.ozner.cup.Guide;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.Login.LoginActivity;
import com.ozner.cup.Login.LoginEnActivity;
import com.ozner.cup.R;
import com.ozner.device.OznerDevice;
import com.ozner.device.OznerDeviceManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by taoran on 2015/11/16.引导页
 * Modify by C-sir@hotmail.com
 */
public class GuideActivity extends Activity {
    private ViewPager viewPager;
    private List<ImageView> list;
    private int[] images = {R.drawable.guide1, R.drawable.guide2, R.drawable.guide3};//引导页图片
    private ImageView[] icons;//用来存储指示性图标的数组
    private int lastposition;
    private boolean isShowLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        super.setContentView(R.layout.activity_guide_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        list = new ArrayList<ImageView>();

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;


        for (int i = 0; i < images.length; i++) {
            ImageView iv = new ImageView(GuideActivity.this);
            //

            InputStream is = getResources().openRawResource(images[i]);
            Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
            BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
            iv.setBackground(bd);

//            iv.setBackgroundResource(images[i]);
            if (i == (images.length - 1)) {
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowNextLoginPage();
                    }
                });
            }
            iv.setScaleType(ImageView.ScaleType.CENTER);
            list.add(iv);
            try {
                for (OznerDevice oznerDevice : OznerDeviceManager.Instance().getDevices()) {
                    OznerDeviceManager.Instance().remove(oznerDevice);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        GuideAdpter adapter = new GuideAdpter(GuideActivity.this, list);
        viewPager.setAdapter(adapter);

        initIcon();//初始化指示图标
        /**
         * 表示当viewpager中pager页发生改变时触发的监听事件
         */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            /*
             * 表示当前的pager页被选中的时候回调的方法
             * int position 表示当前被选中pager页的下标
             */
            @Override
            public void onPageSelected(int position) {
                //Log.i(TAG, "onPageSelected------position------"+position);
                //通过循环的形式将所有的指示图标变成灰色  然后根据当前pager页的下标将选中的置为白色
                for (int i = 0; i < images.length; i++) {
                    icons[i].setEnabled(true);
                }
                icons[position].setEnabled(false);

            }

            /*
             * 表示当前pager页滑动时回调的方法
             * int position,  表示当前滑动的pager页滚动的下标
             * float positionOffset, 表示当前滑动的pager页的偏移量  [0,1}
             * int positionOffsetPixels 表示当前滑动的pager页的偏移量的像素值
             */
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//				Log.i(TAG, "onPageScrolled------position------"+position);
//				Log.i(TAG, "positionOffset------------"+positionOffset);
//				Log.i(TAG, "positionOffsetPixels------------"+positionOffsetPixels);
                Log.i("tag", "onPageScrolled------position------" + position);
                if (lastposition == position && lastposition == images.length - 1) {
//                    Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
//                    GuideActivity.this.startActivity(intent);
//                    GuideActivity.this.finish();
                    ShowNextLoginPage();
                }
                lastposition = position;

            }

            /*
             * 表示当前viewpager中的pager页状态发生改变时回调的方法
             * SCROLL_STATE_IDLE     停止滑动状态
               SCROLL_STATE_DRAGGING 正在滑动状态
               SCROLL_STATE_SETTLING 选中状态
             */
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
                int pageWidth = view.getWidth();

                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    view.setAlpha(0);

                } else if (position <= 0) { // [-1,0]
                    // Use the default slide transition when moving to the left page
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setScaleX(1);
                    view.setScaleY(1);

                } else if (position <= 1) { // (0,1]
                    // Fade the page out.
                    view.setAlpha(1 - position);

                    // Counteract the default slide transition
                    view.setTranslationX(pageWidth * -position);

                    // Scale the page down (between MIN_SCALE and 1)
                    float scaleFactor = 0.75f
                            + (1 - 0.75f) * (1 - Math.abs(position));
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);

                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    view.setAlpha(0);
                }

            }
        });
    }

    private void ShowNextLoginPage() {
        if (!isShowLogin) {
            Intent intent = new Intent();
            if (((OznerApplication) getApplication()).isLanguageCN()) {
                intent.setClass(GuideActivity.this, LoginActivity.class);
            } else {
                intent.setClass(GuideActivity.this, LoginEnActivity.class);
            }
            GuideActivity.this.startActivity(intent);
            GuideActivity.this.finish();
            isShowLogin = true;
        }
    }
    /*
     * 声明方法初始化指示图标
	 */

    public void initIcon() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        icons = new ImageView[images.length];

        for (int i = 0; i < images.length; i++) {
            icons[i] = (ImageView) layout.getChildAt(i);//根据imageview的下标初始化imageview数组中的每一个元素
            icons[i].setEnabled(true);//跟drawable/dot.xml文件相关的状态设置
            icons[i].setTag(i);
            //为每个指示性imageview图标添加单击事件
            icons[i].setOnClickListener(new View.OnClickListener() {

                @Override    //imageview1   0    imageview2   1   imageview3  2
                public void onClick(View v) {//View v=imageview2;
                    //根据参数指定的item的下标设置当前的viewpager中选中的pager页
                    viewPager.setCurrentItem((Integer) v.getTag());
                }
            });
        }
        icons[0].setEnabled(false);//初始化时默认选中第一个pager页显示 白色指示图标
    }

    @Override
    protected void onStop() {
        System.gc();
        super.onStop();
    }
}
