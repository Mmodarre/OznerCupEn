package com.ozner.yiquan.Device;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ozner.yiquan.R;

/**
 * Created by mengdongya on 2016/1/9.
 */
public class CommonProblemActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_problem);
        OznerApplication.changeTextFont((ViewGroup)getWindow().getDecorView());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.toolbar_text)).setText(getString(R.string.common_problem));
        ((ImageView) findViewById(R.id.iv_common_problem)).setImageResource(R.drawable.common_problem);

    }
}
