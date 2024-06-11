package com.example.myapplication.view;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MessageReceiver;
import com.example.myapplication.NetworkService;
import com.example.myapplication.R;
import com.example.myapplication.activity.ProfileActivity;
import com.example.myapplication.adapter.RoomAdapter;
import com.example.myapplication.entity.Room;
import com.example.myapplication.util.DPIUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements MessageReceiver {
    private ImageButton avatarButton;
    private TextView idTextView;

    private RecyclerView mRecyclerView;

    private RoomAdapter mRoomAdapter;
    List<Room> mRooms = new ArrayList<>();
    public ChatFragment(List<Room> rooms){
        this.mRooms=rooms;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        //id
        idTextView = view.findViewById(R.id.usernameTextView);

        //个人信息按钮
        avatarButton = view.findViewById(R.id.avatarButton);
        avatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                getActivity().startActivity(intent);
            }
        });
        // 初始化转换工具
        DPIUtil.setDensity(getResources().getDisplayMetrics().density);

        mRecyclerView = view.findViewById(R.id.roomRecyclerView);
        View root = view.findViewById(R.id.customLinearLayout);
        if (root instanceof CustomLinearLayout) {
            CustomLinearLayout cll = (CustomLinearLayout) root;
            cll.setOnTouchListener(new CustomLinearLayout.OnTouchListener() {
                @Override
                public void doTouch(Point point) {
                    if (mRoomAdapter != null) {
                        mRoomAdapter.restoreItemView(point);
                    }
                }
            });
        }
        //初始化adapter
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRoomAdapter = new RoomAdapter(getActivity(), mRecyclerView, mRooms);
        mRecyclerView.setAdapter(mRoomAdapter);

        return view;
    }
    @Override
    public void receiveMessage(String messageJson) {
        // 更新RecyclerView的数据
        JsonArray dataArray = JsonParser.parseString(messageJson).getAsJsonArray();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Room>>() {}.getType();
        mRooms = gson.fromJson(dataArray, listType);

        // 通知adapter数据已经改变
        mRoomAdapter.notifyDataSetChanged();
    }
}