package com.ozner.yiquan.slideleft;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ozner.yiquan.MainActivity;

/**
 * Created by admin on 2015/11/26.
 */
public class BaseFragment   extends Fragment {
    public MainActivity context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        context = (MainActivity) getActivity();
    }

}
