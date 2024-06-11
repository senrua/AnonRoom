package com.example.myapplication.activity;

import android.annotation.SuppressLint;
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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.NetworkService;
import com.example.myapplication.R;
import com.example.myapplication.entity.Contact;
import com.example.myapplication.entity.Room;
import com.example.myapplication.view.ChatFragment;
import com.example.myapplication.view.ContactFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageButton chatButton;
    private ImageButton contactButton;
    private NetworkService networkService;
    private Fragment mChatFragment;
    private Fragment mContactFragment;
    private boolean isBound = false;
    private List<Room> mRooms = new ArrayList<>();
    private List<Contact> mContacts = new ArrayList<>();
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
                List<Room> rooms = (List<Room>) msg.obj;
                SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String roomsJson = gson.toJson(rooms);
                sendMessageToChatFragment(roomsJson);
                editor.putString("rooms", roomsJson);
                editor.apply();
            } else if (msg.what == 2) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            } else if (msg.what == 3) {
                List<Contact> contacts = (List<Contact>) msg.obj;
                SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String contactsJson = gson.toJson(contacts);
                sendMessageToContactFragment(contactsJson);
                editor.putString("contacts", contactsJson);
                editor.apply();
            } else if (msg.what == 4) {
                Exception e = (Exception) msg.obj;
                // 处理错误
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String roomsJson=sharedPreferences.getString("roomsJson",null);
        String contactsJson=sharedPreferences.getString("contacts",null);
        Gson gson = new Gson();

        if (roomsJson != null) {
            Type roomListType = new TypeToken<ArrayList<Room>>(){}.getType();
            mRooms = gson.fromJson(roomsJson, roomListType);
        }

        if (contactsJson != null) {
            Type contactListType = new TypeToken<ArrayList<Contact>>(){}.getType();
            mContacts = gson.fromJson(contactsJson, contactListType);
        }
        mChatFragment=new ChatFragment(mRooms);
        mContactFragment=new ContactFragment(mContacts);

        // 更新数据
        if (isBound) {
            networkService.getUserHomeList(username);
            networkService.getUserFriendList(username);
        }

        chatButton = findViewById(R.id.chatButton);
        contactButton = findViewById(R.id.contactButton);


        String fragment = getIntent().getStringExtra("fragment");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // 添加两个Fragment
        fragmentTransaction.add(R.id.fragment_container, mChatFragment,"ChatFragment");
        fragmentTransaction.add(R.id.fragment_container, mContactFragment,"ContactFragment");

        if ("chat".equals(fragment)) {
            fragmentTransaction.hide(mContactFragment);
            chatButton.setSelected(true);
            contactButton.setSelected(false);
        } else if ("contact".equals(fragment)) {
            // 隐藏其中一个Fragment
            fragmentTransaction.hide(mChatFragment);
            chatButton.setSelected(false);
            contactButton.setSelected(true);
        }
        // 提交事务
        fragmentTransaction.commit();

        GestureDetector chatGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // 当点击 chatButton 时，显示 ChatFragment
                getSupportFragmentManager().beginTransaction()
                        .hide(mContactFragment).show(mChatFragment)
                        .commit();
                chatButton.setSelected(true);
                contactButton.setSelected(false);
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isBound) {
                    networkService.getUserHomeList(username);
                }
                return super.onDoubleTap(e);
            }
        });

        // 设置chatButton的onTouchListener
        chatButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 将触摸事件传递给gestureDetector
                return chatGestureDetector.onTouchEvent(event);
            }
        });
        GestureDetector contactGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // 当点击 contactButton 时，显示 ContactFragment
                getSupportFragmentManager().beginTransaction()
                        .hide(mChatFragment).show(mContactFragment)
                        .commit();

                chatButton.setSelected(false);
                contactButton.setSelected(true);
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isBound) {
                    networkService.getFriendInfo(username);
                }
                return super.onDoubleTap(e);
            }
        });

        // 设置contactButton的onTouchListener
        contactButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 将触摸事件传递给contactGestureDetector
                boolean result = contactGestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return result;
            }
        });
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public boolean isServiceBound() {
        return isBound;
    }
    public void sendMessageToChatFragment(String messageJson) {
        // 获取Fragment的实例
        FragmentManager fragmentManager = getSupportFragmentManager();
        ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentByTag("ChatFragment");
        // 如果Fragment已经添加到Activity
        if (chatFragment != null ) {
            chatFragment.receiveMessage(messageJson);
        }
    }
    public void sendMessageToContactFragment(String messageJson) {
        // 获取Fragment的实例
        FragmentManager fragmentManager = getSupportFragmentManager();
        ContactFragment contactFragment = (ContactFragment) fragmentManager.findFragmentByTag("ContactFragment");
        // 如果Fragment已经添加到Activity
        if (contactFragment != null ) {
            contactFragment.receiveMessage(messageJson);
        }
    }
}