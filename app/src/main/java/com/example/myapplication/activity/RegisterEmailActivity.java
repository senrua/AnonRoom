package com.example.myapplication.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.NetworkService;
import com.example.myapplication.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

public class RegisterEmailActivity extends AppCompatActivity {
    private TextView backTextView;
    private TextView nextTextView;
    private EditText emailEditText;
    private Spinner emailSpinner;
    private NetworkService networkService;
    private boolean isBound = false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
            networkService = binder.getService();
            networkService.setHandler(handler);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                // 处理验证码请求成功的情况
                String result = (String) msg.obj;
                Intent intent = new Intent(RegisterEmailActivity.this, RegisterVerificationActivity.class);
                intent.putExtra("mailbox", result);
                startActivity(intent);
            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                Toast.makeText(RegisterEmailActivity.this, "请求验证码失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);

        emailEditText = findViewById(R.id.emailEditText);
        backTextView = findViewById(R.id.backTextView);
        nextTextView = findViewById(R.id.nextTextView);
        emailSpinner = findViewById(R.id.emailSpinner);

        nextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString()+ emailSpinner.getSelectedItem().toString();
                //验证邮箱是否合法
                if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                    Toast.makeText(RegisterEmailActivity.this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                networkService.requestEmailVerificationCode(email);
            }
        });
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回登陆界面
                Intent intent = new Intent(RegisterEmailActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}