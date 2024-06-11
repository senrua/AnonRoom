package com.example.myapplication.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

public class FriendProfileActivity extends AppCompatActivity {
    private TextView backTextView;
    private ImageView avatarImageView;
    private TextView nicknameTextView;
    private TextView userStatusTextView;
    private TextView usernameTextView;
    private TextView mailboxTextView;
    private TextView summaryTextView;
    private TextView deleteFriendButton;
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
                if (userInfo.getUser_status() == 1) {
                    userStatusTextView.setText("在线");
                    userStatusTextView.setTextColor(Color.GREEN);
                }
                // 更新头像 UI
                if(!avatarPath.equals(""))
                    Picasso.get().load(new File(avatarPath)).into(avatarImageView);
                else
                    Picasso.get().load(R.drawable.failed).into(avatarImageView);
            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            } else if (msg.what == 3) {
                // 返回好友界面
                Intent intent = new Intent(FriendProfileActivity.this, MainActivity.class);
                intent.putExtra("fragment", "contact");
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
        userStatusTextView = findViewById(R.id.userStatusTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        mailboxTextView = findViewById(R.id.mailboxLabelTextView);
        summaryTextView = findViewById(R.id.summaryTextView);
        // 获取传递过来的 friend_username
        String friendUsername = getIntent().getStringExtra("friend_username");
        //获取用户名
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        networkService.getFriendInfo(friendUsername);
        //返回按钮
        backTextView = findViewById(R.id.backTextView);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendProfileActivity.this, MainActivity.class);
                intent.putExtra("fragment", "contact");
                startActivity(intent);
            }
        });
        //删除好友按钮
        deleteFriendButton = findViewById(R.id.deleteFriendButton);
        deleteFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkService.deleteFriend(username, friendUsername);
            }
        });
    }
}
