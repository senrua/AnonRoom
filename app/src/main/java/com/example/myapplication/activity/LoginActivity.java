package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Handler;
import android.view.View;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.NetworkService;
import com.example.myapplication.R;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText verificationCodeEditText;
    private Button sendverificationCodeButton;
    private Button loginButton;
    private TextView registerTextView;
    private NetworkService networkService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
            networkService = binder.getService();
            isBound = true;
            networkService.setHandler(handler);
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
        startService(intent);
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
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                Toast.makeText(LoginActivity.this, "已发送验证码", Toast.LENGTH_SHORT).show();
            }else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理登录失败的情况
                Toast.makeText(LoginActivity.this, "验证码获取失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            else if (msg.what == 3) {
                HashMap<String, String> userData = (HashMap<String, String>) msg.obj;
                String username = userData.get("username");
                String password = userData.get("password");
                // 处理登录成功的情况
                // 跳转到主页面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == 4) {
                Exception e = (Exception) msg.obj;
                // 处理登录失败的情况
                Toast.makeText(LoginActivity.this, "登陆失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 检查用户的登录状态
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);
        if (isLogin) {
            // 如果用户已经登录，那么直接跳转到主页面
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        sendverificationCodeButton = findViewById(R.id.sendVerificationCodeButton);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        sendverificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sendverificationCodeButton.isEnabled()) {
                    Toast.makeText(LoginActivity.this, "请在一分钟后再试", Toast.LENGTH_SHORT).show();
                    return;
                }

                String email = emailEditText.getText().toString();
                //验证邮箱是否合法
                if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                    Toast.makeText(LoginActivity.this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                networkService.requestEmailVerificationCode(email);

                // 禁用按钮
                sendverificationCodeButton.setEnabled(false);

                // 启动一个计时器，当计时器结束时，再次启用按钮
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendverificationCodeButton.setEnabled(true);
                    }
                }, 60000); // 60000毫秒后执行，即60秒后执行
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    String username = usernameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String email = emailEditText.getText().toString();
                    String code =verificationCodeEditText.getText().toString();
                    // 验证所有字段都不为空
                    if (username.isEmpty() || password.isEmpty() || email.isEmpty() || code.isEmpty()) {
                        Toast.makeText(LoginActivity.this, "未填写完整", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 验证邮箱格式
                    if (!email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                        Toast.makeText(LoginActivity.this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 验证验证码为四位数字
                    if (!code.matches("^\\d{4}$")) {
                        Toast.makeText(LoginActivity.this, "验证码必须为四位数字", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    networkService.login(username, password,email,code);
                } else {
                    Toast.makeText(LoginActivity.this, "服务未绑定", Toast.LENGTH_SHORT).show();
                }
            }
        });
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterEmailActivity.class);
                startActivity(intent);
            }
        });
    }


}