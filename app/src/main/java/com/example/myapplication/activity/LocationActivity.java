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
import com.example.myapplication.adapter.LocationAdapter;
import com.example.myapplication.entity.Location;
import com.example.myapplication.entity.NewFriend;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity implements LocationAdapter.OnAddButtonClickListener{

    private TextView backTextView;
    private TextView quitTextView;
    private RecyclerView mRecyclerView;
    private List<Location> mLocations = new ArrayList<>();
    private LocationAdapter mLocationAdapter;
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
                Toast.makeText(LocationActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            } else if (msg.what == 3) {
                mLocations= (List<Location>) msg.obj;
                // 通知adapter数据已经改变
                mLocationAdapter.notifyDataSetChanged();
            } else if (msg.what == 4) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            } else if (msg.what == 5) {
                mLocations= (List<Location>) msg.obj;
                // 通知adapter数据已经改变
                mLocationAdapter.notifyDataSetChanged();
            } else if (msg.what == 6) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
        networkService.addLocation(username);
        backTextView.findViewById(R.id.backTextView);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LocationActivity.this, MainActivity.class);
                intent.putExtra("fragment", "contact");
                startActivity(intent);
            }
        });
        quitTextView.findViewById(R.id.quitTextView);
        quitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mRecyclerView = findViewById(R.id.LocationRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLocationAdapter = new LocationAdapter(this, mRecyclerView, mLocations);
        mLocationAdapter.setOnConfirmButtonClickListener(this);
        mRecyclerView.setAdapter(mLocationAdapter);
    }

    @Override
    public void onAddButtonClick(String targetName) {
        networkService.addFriend(username,targetName);
    }


}