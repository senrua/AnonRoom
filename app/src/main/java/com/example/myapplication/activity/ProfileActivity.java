package com.example.myapplication.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.NetworkService;
import com.example.myapplication.R;
import com.example.myapplication.entity.UserInfo;
import com.example.myapplication.util.ImageUtil;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ProfileActivity extends AppCompatActivity {
    private TextView backTextView;
    private TextView editTextView;
    private ImageView avatarImageView;
    private TextView nicknameTextView;
    private TextView userStatusTextView;
    private TextView usernameTextView;
    private TextView mailboxTextView;
    private TextView summaryTextView;
    private TextView logoutButton;
    private NetworkService networkService;
    private boolean isBound = false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
            networkService = binder.getService();
            networkService.setHandler(handler);
            isBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
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
            // 处理从 Service 发送过来的消息
            if (msg.what == 1) {
                Pair<UserInfo, String> result = (Pair<UserInfo, String>) msg.obj;
                UserInfo userInfo = result.first;
                String avatarPath = result.second;
                // 使用获取的数据来更新 UI
                nicknameTextView.setText(userInfo.getNickname());
                usernameTextView.setText(userInfo.getUsername());
                mailboxTextView.setText(userInfo.getMailbox());
                summaryTextView.setText(userInfo.getSummary());
                if(userInfo.getUser_status()==1){
                    userStatusTextView.setText("在线");
                    userStatusTextView.setTextColor(Color.GREEN);
                }
                if(!avatarPath.equals(""))
                    Picasso.get().load(new File(avatarPath)).into(avatarImageView);
                else
                    Picasso.get().load(R.drawable.failed).into(avatarImageView);
                // 将用户信息和是否已经获取到个人信息的状态存储到 SharedPreferences 中
                SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                editor.putString("userInfo", gson.toJson(userInfo));
                editor.putBoolean("isUserInfoFetched", true);
                editor.apply();
            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            }else if(msg.what == 3){
                // 清除 SharedPreferences 中的用户信息
                SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                // 关闭 NetworkService
                if (isBound) {
                    unbindService(connection);
                    isBound = false;
                }
                // 完全停止 NetworkService
                Intent intent = new Intent(ProfileActivity.this, NetworkService.class);
                stopService(intent);
                // 返回登录界面
                intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == 4) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        avatarImageView = findViewById(R.id.avatarImageView);
        nicknameTextView = findViewById(R.id.nicknameTextView);
        userStatusTextView =findViewById(R.id.userStatusTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        mailboxTextView = findViewById(R.id.mailboxLabelTextView);
        summaryTextView = findViewById(R.id.summaryTextView);
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        boolean isUserInfoFetched = sharedPreferences.getBoolean("isUserInfoFetched", false);

        if (isUserInfoFetched) {
            String userInfoJson = sharedPreferences.getString("userInfo", null);
            Gson gson = new Gson();
            UserInfo userInfo = gson.fromJson(userInfoJson, UserInfo.class);

            if (userInfo.getUsername().equals(username)) {
                // 使用获取的数据来更新 UI
                nicknameTextView.setText(userInfo.getNickname());
                usernameTextView.setText(userInfo.getUsername());
                mailboxTextView.setText(userInfo.getMailbox());
                summaryTextView.setText(userInfo.getSummary());
                if(userInfo.getUser_status()==1){
                    userStatusTextView.setText("在线");
                    userStatusTextView.setTextColor(Color.GREEN);
                }
                // 更新头像 UI
                String avatarImagePath = ImageUtil.getAvatarImagePath(this, username);
                if (avatarImagePath != null) {
                    Picasso.get().load(new File(avatarImagePath)).into(avatarImageView);
                }
                else
                    Picasso.get().load(R.drawable.failed).into(avatarImageView);

            } else {
                networkService.getPersonalInfo(username);
            }
        } else {
            networkService.getPersonalInfo(username);
        }
        //返回按钮
        backTextView = findViewById(R.id.backTextView);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("fragment", "chat");
                startActivity(intent);
            }
        });
        //编辑按钮
        editTextView=findViewById(R.id.editTextView);
        editTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileActivity.this,EditActivity.class);
                startActivity(intent);
            }
        });
        //注销按钮
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用 NetworkService 的 logout 方法来注销用户
                networkService.logout(username);
            }
        });
    }
}