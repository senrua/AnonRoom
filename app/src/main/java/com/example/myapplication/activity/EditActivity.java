package com.example.myapplication.activity;

import static com.example.myapplication.util.ImageUtil.saveAvatarImage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.NetworkService;
import com.example.myapplication.R;
import com.example.myapplication.entity.UserInfo;
import com.example.myapplication.util.ImageUtil;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private ImageView avatarImageView;
    private EditText nicknameEditText;
    private EditText summaryEditText;
    private TextView backTextView;
    private TextView confirmTextView;
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
                SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isUserInfoFetched", false);
                editor.apply();
                Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
                finish();

            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        avatarImageView = findViewById(R.id.avatarImageView);
        nicknameEditText=findViewById(R.id.nicknameEditText);
        summaryEditText=findViewById(R.id.summaryEditText);
        backTextView = findViewById(R.id.backTextView);
        confirmTextView = findViewById(R.id.confirmTextView);
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        boolean isUserInfoFetched = sharedPreferences.getBoolean("isUserInfoFetched", false);

        if (isUserInfoFetched) {
            String userInfoJson = sharedPreferences.getString("userInfo", null);
            Gson gson = new Gson();
            UserInfo userInfo = gson.fromJson(userInfoJson, UserInfo.class);

            if (userInfo.getUsername().equals(username)) {
                // 使用获取的数据来更新 UI
                nicknameEditText.setText(userInfo.getNickname());
                summaryEditText.setText(userInfo.getSummary());
                // 更新头像 UI
                String avatarImagePath = ImageUtil.getAvatarImagePath(this, username);
                if (avatarImagePath != null) {
                    Picasso.get().load(new File(avatarImagePath)).into(avatarImageView);
                } else
                    Picasso.get().load(R.drawable.failed).into(avatarImageView);

            }
        }
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });

        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
                finish();
            }
        });

        confirmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的新的昵称和简介
                String newNickname = nicknameEditText.getText().toString();
                String newSummary = summaryEditText.getText().toString();

                // 获取新选择的头像
                avatarImageView.setDrawingCacheEnabled(true);
                Bitmap bitmap = avatarImageView.getDrawingCache();
                saveAvatarImage(EditActivity.this,bitmap,username);
                String newAvatarPicture = ImageUtil.bitmapToString(bitmap);

                // 将新的信息发送到服务器
                networkService.changePersonalInfo(username, newNickname, newAvatarPicture, newSummary);
            }
        });
    }

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // 处理返回的Uri
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        // 将bitmap转换为PNG并设置到ImageView
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        avatarImageView.setImageBitmap(decodedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

}
