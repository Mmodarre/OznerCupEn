package com.ozner.qianye.WaterCup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozner.qianye.Device.OznerApplication;
import com.ozner.qianye.R;

/**
 * Created by taoran on 2015/12/3.
 */
public class TDSIntroduceFragment extends Fragment {
    private Toolbar toolbar;


    public TDSIntroduceFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.activity_tds_text, container, false);
        OznerApplication.changeTextFont((ViewGroup)view);
        toolbar=(Toolbar)view.findViewById(R.id.introduce_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("on", "dddd");
                getFragmentManager().popBackStack();
            }
        });
//        tdsTextBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                getFragmentManager().beginTransaction().replace(R.id.framen_main_con, new TDSFragment()).commit();
////                getFragmentManager().beginTransaction().addToBackStack(null);
////                getSupportFragmentManager().popBackStack();
//                Log.e("on", "dddd");
//                getFragmentManager().popBackStack();
//            }
//        });
        return view;
    }
}