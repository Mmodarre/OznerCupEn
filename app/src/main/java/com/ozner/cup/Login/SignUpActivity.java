package com.ozner.cup.Login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ozner.cup.R;

public class SignUpActivity extends AppCompatActivity {
    Button btn_sign_up;
    MyClickListener clickListener = new MyClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        btn_sign_up = (Button)findViewById(R.id.btn_sign_up);
        btn_sign_up.setOnClickListener(clickListener);
    }

    class MyClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_sign_up:
                    Toast.makeText(SignUpActivity.this, "注册账号", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
