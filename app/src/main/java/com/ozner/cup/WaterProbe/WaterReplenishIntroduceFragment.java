package com.ozner.cup.WaterProbe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ozner.cup.Device.OznerApplication;
import com.ozner.cup.R;

/**
 * Created by taoran on 2015/12/3.
 */
public class WaterReplenishIntroduceFragment extends Fragment {
    private Toolbar toolbar;
    private TextView introduce_toolbar_text;


    public WaterReplenishIntroduceFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.waterreplenish_introduce_text, container, false);
        OznerApplication.changeTextFont((ViewGroup)view);
        introduce_toolbar_text=(TextView)view.findViewById(R.id.introduce_toolbar_text);
        introduce_toolbar_text.setText(R.string.water_replenish_titleHum);
        toolbar=(Toolbar)view.findViewById(R.id.introduce_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getFragmentManager().popBackStack();
            }
        });

        TextView textView = (TextView)view.findViewById(R.id.textValue);
        textView.setText(String.format(getString(R.string.water_replenish_humSeasonValue),"3%","5%"));
        return view;
    }
}