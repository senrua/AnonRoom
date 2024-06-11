package com.example.myapplication.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.NetworkService;
import com.example.myapplication.R;
import com.example.myapplication.adapter.NewFriendAdapter;
import com.example.myapplication.entity.Contact;
import com.example.myapplication.entity.NewFriend;
import com.example.myapplication.entity.Room;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class NewFriendActivity extends AppCompatActivity implements NewFriendAdapter.OnConfirmButtonClickListener {

    private EditText usernameEditText;
    private Button addFriendButton;
    private TextView backTextView;
    private RecyclerView mRecyclerView;
    private List<NewFriend> mNewFriends = new ArrayList<>();
    private NewFriendAdapter mNewFriendAdapter;
    private boolean isBound = false;
    private NetworkService networkService;
    private String username;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
            networkService = binder.getService();
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

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(NewFriendActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            } else if (msg.what == 3) {
                networkService.getRequestFriendList(username);
            } else if (msg.what == 4) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            } else if (msg.what == 5) {
                mNewFriends= (List<NewFriend>) msg.obj;
                // 通知adapter数据已经改变
                mNewFriendAdapter.notifyDataSetChanged();
            } else if (msg.what == 6) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        usernameEditText = findViewById(R.id.usernameEditText);
        addFriendButton = findViewById(R.id.addFriendButton);
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
        networkService.getRequestFriendList(username);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                if (!username.isEmpty()) {
                    String targetName = usernameEditText.getText().toString();
                    networkService.addFriend(username, targetName);
                    Toast.makeText(NewFriendActivity.this, "已发送请求", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewFriendActivity.this, "用户名不为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        backTextView.findViewById(R.id.backTextView);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewFriendActivity.this, MainActivity.class);
                intent.putExtra("fragment", "contact");
                startActivity(intent);
            }
        });
        mRecyclerView = findViewById(R.id.newFriendRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNewFriendAdapter = new NewFriendAdapter(this, mRecyclerView, mNewFriends);
        mNewFriendAdapter.setOnConfirmButtonClickListener(this);
        mRecyclerView.setAdapter(mNewFriendAdapter);
    }

    @Override
    public void onConfirmButtonClick(String targetName, String isAgree) {
        networkService.ensureFriend(username, isAgree, targetName);
    }

}