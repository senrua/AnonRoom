package com.example.myapplication.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.NetworkService;
import com.example.myapplication.R;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterVerificationActivity extends AppCompatActivity {

    private EditText verificationCodeEditText1;
    private EditText verificationCodeEditText2;
    private EditText verificationCodeEditText3;
    private EditText verificationCodeEditText4;
    private EditText usernameEditText;
    private EditText nicknameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private TextView backTextView;
    private TextView submitTextView;
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
            if (msg.what == 1) {
                Intent intent = new Intent(RegisterVerificationActivity.this, LoginActivity.class);
                Toast.makeText(RegisterVerificationActivity.this, "注册成功" , Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理注册失败的情况
                Toast.makeText(RegisterVerificationActivity.this, "注册失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_verification);
        usernameEditText = findViewById(R.id.usernameEditText);
        nicknameEditText = findViewById(R.id.nicknameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        verificationCodeEditText1 = findViewById(R.id.verificationCodeEditText1);
        verificationCodeEditText2 = findViewById(R.id.verificationCodeEditText2);
        verificationCodeEditText3 = findViewById(R.id.verificationCodeEditText3);
        verificationCodeEditText4 = findViewById(R.id.verificationCodeEditText4);
        backTextView = findViewById(R.id.backTextView);
        submitTextView = findViewById(R.id.submitTextView);

        verificationCodeEditText1.addTextChangedListener(new VerificationCodeTextWatcher(verificationCodeEditText1, verificationCodeEditText2));
        verificationCodeEditText2.addTextChangedListener(new VerificationCodeTextWatcher(verificationCodeEditText2, verificationCodeEditText3));
        verificationCodeEditText3.addTextChangedListener(new VerificationCodeTextWatcher(verificationCodeEditText3, verificationCodeEditText4));
        verificationCodeEditText4.addTextChangedListener(new VerificationCodeTextWatcher(verificationCodeEditText4, null));


        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCode = verificationCodeEditText1.getText().toString() +
                        verificationCodeEditText2.getText().toString() +
                        verificationCodeEditText3.getText().toString() +
                        verificationCodeEditText4.getText().toString();
                String username = usernameEditText.getText().toString();
                String nickname = nicknameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                String mailbox = getIntent().getStringExtra("mailbox");
                // 验证验证码是否正确
                if (!mailbox.equals(verificationCode)) {
                    Toast.makeText(RegisterVerificationActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 验证昵称是否为空
                if (nickname.isEmpty()) {
                    Toast.makeText(RegisterVerificationActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 验证密码和确认密码是否匹配
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterVerificationActivity.this, "密码和确认密码不匹配", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isBound) {
                    // 获取当前的日期和时间
                    Date now = new Date();
                    // 创建一个 SimpleDateFormat 对象
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // 将日期和时间格式化为字符串
                    String created_time = formatter.format(now);

                    // 获取 Drawable 对象
                    Drawable drawable = getResources().getDrawable(R.drawable.anon_group, null);
                    // 将 Drawable 对象转换为 Bitmap 对象
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    // 创建一个字节数组输出流
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    // 将 Bitmap 对象压缩到字节数组输出流
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    // 将字节数组输出流转换为字节数组
                    byte[] byteArray = baos.toByteArray();
                    // 将字节数组编码为 Base64 字符串
                    String avatar_picture = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    networkService.register(created_time, username, password, nickname, avatar_picture, mailbox, verificationCode);
                } else {
                    Toast.makeText(RegisterVerificationActivity.this, "服务未绑定", Toast.LENGTH_SHORT).show();
                }
            }
        });
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterVerificationActivity.this, RegisterEmailActivity.class);
                startActivity(intent);
            }
        });
    }
    private static class VerificationCodeTextWatcher implements TextWatcher {

        private final EditText currentEditText;
        private final EditText nextEditText;

        VerificationCodeTextWatcher(EditText currentEditText, EditText nextEditText) {
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextEditText != null) {
                nextEditText.requestFocus();
            }
        }
    }
}