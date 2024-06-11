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
import com.example.myapplication.adapter.ContactAdapter;
import com.example.myapplication.entity.Contact;
import com.example.myapplication.entity.Room;
import com.example.myapplication.util.DPIUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment implements MessageReceiver {

    private RecyclerView mRecyclerView;

    private ContactAdapter mContactAdapter;
    private NetworkService networkService;
    List<Contact> mContacts = new ArrayList<>();
    public ContactFragment(List<Contact> contacts){
        this.mContacts=contacts;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        
        mRecyclerView = view.findViewById(R.id.contactRecyclerView);

        //初始化adapter
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactAdapter = new ContactAdapter(getActivity(), mRecyclerView, mContacts);
        mRecyclerView.setAdapter(mContactAdapter);

        return view;
    }
    @Override
    public void receiveMessage(String messageJson) {
        // 更新RecyclerView的数据
        JsonArray dataArray = JsonParser.parseString(messageJson).getAsJsonArray();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Contact>>() {}.getType();
        mContacts = gson.fromJson(dataArray, listType);

        // 通知adapter数据已经改变
        mContactAdapter.notifyDataSetChanged();
    }
}
