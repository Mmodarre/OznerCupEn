package com.ozner.cup.Device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ozner.cup.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xinde on 2015/12/14.
 */
public class SpinnerPopWindow extends PopupWindow implements AdapterView.OnItemClickListener {
    private Context mContext;
    private ListView mListView;
    private SpinnerPopAdapter mAdapter;
    private IOnItemSelectListener mItemSelectListener;


    public interface IOnItemSelectListener {
        void onItemClick(int pos);
    }

    public SpinnerPopWindow(Context context) {
        super(context);
        mContext = context;
        init();
    }


    public void setItemListener(IOnItemSelectListener listener) {
        mItemSelectListener = listener;
    }


    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.spiner_window_layout, null);
        setContentView(view);
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(null);
        setOutsideTouchable(true);

        mListView = (ListView) view.findViewById(R.id.lv_valuelist);


        mAdapter = new SpinnerPopAdapter(mContext);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }


    public void refreshData(List<String> list) {
        if (list != null) {
            mAdapter.refreshData(list);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
        dismiss();
        if (mItemSelectListener != null) {
            mItemSelectListener.onItemClick(pos);
        }
    }


    class SpinnerPopAdapter extends BaseAdapter {
        private List<String> datalist = new ArrayList<String>();
        private Context mContext;
        private LayoutInflater mInflater;

        public SpinnerPopAdapter(Context context) {
            this.mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        public void refreshData(List<String> datalist) {
            this.datalist = datalist;

            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return datalist.size();
        }

        @Override
        public Object getItem(int position) {
            return datalist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.spinner_dropdown_list_item, null);
                viewHolder.tv_text = (TextView) convertView.findViewById(R.id.tv_text);
//                viewHolder.rlay_lockImg = (RelativeLayout) convertView.findViewById(R.id.rlay_lockImg);
//                viewHolder.iv_wifiImg = (ImageView) convertView.findViewById(R.id.iv_wifiImg);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tv_text.setText(datalist.get(position));
            return convertView;
        }

        private class ViewHolder {
            public TextView tv_text;
        }
    }

    public static class DropDownItem {
        public String text;
//        public boolean islocked;
//        public int wifiImgId;
    }
}
