package com.ozner.qianye.Guide;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import java.util.List;


/**
 * Created by taoran on 2015/11/16.引导页适配器
 */

public class GuideAdpter extends PagerAdapter {

	private List<ImageView> viewList;
	private Context mcontext;


	public GuideAdpter(Context context,List<ImageView> list){
         mcontext=context;
		viewList=list;

	}

	public int getCount() {
		return viewList.size();
	}

	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(viewList.get(position), 0);
		return viewList.get(position);
	}

	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(viewList.get(position));
	}

}
