package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.FriendProfileActivity;
import com.example.myapplication.activity.NewFriendActivity;
import com.example.myapplication.entity.Contact;
import com.example.myapplication.entity.NewFriend;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class NewFriendAdapter extends RecyclerView.Adapter{
    private Context mContext;

    private RecyclerView mRecyclerView;

    private List<NewFriend> mNewFriend;
    public NewFriendAdapter(Context context, RecyclerView recyclerView, List<NewFriend> newFriend) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mNewFriend = newFriend;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactLinearLayout= inflater.inflate(R.layout.layout_new_friend, null);
        return new ContactAdapter.MyVH(contactLinearLayout);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NewFriend newFriend=mNewFriend.get(position);
        MyVH myVH = (MyVH) holder;
        myVH.nicknameTextView.setText(newFriend.getNickname());
        myVH.usernameTextView.setText(newFriend.getUsername());

        String avatarImagePath=newFriend.getAvatarPath();
        if (avatarImagePath != null) {
            Picasso.get().load(new File(avatarImagePath)).into(myVH.userAvatarImageView);
        }
        myVH.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = newFriend.getUsername();
                listener.onConfirmButtonClick(username,"1");
                myVH.confirmButton.setEnabled(false);
                myVH.rejectButton.setEnabled(false);
            }
        });
        myVH.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = newFriend.getUsername();
                listener.onConfirmButtonClick(username,"0");
                myVH.confirmButton.setEnabled(false);
                myVH.rejectButton.setEnabled(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNewFriend.size();
    }

    public static class MyVH extends RecyclerView.ViewHolder {
        public TextView nicknameTextView;
        public TextView usernameTextView;
        public ImageView userAvatarImageView;
        public Button confirmButton;
        public Button rejectButton;

        public MyVH(@NonNull View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.nickname);
            usernameTextView = itemView.findViewById(R.id.username);
            confirmButton=itemView.findViewById(R.id.confirmButton);
            rejectButton=itemView.findViewById(R.id.rejectButton);
            userAvatarImageView = itemView.findViewById(R.id.user_avatar);
        }
    }
    public interface OnConfirmButtonClickListener {
        void onConfirmButtonClick(String targetName,String isAgree);
    }
    private OnConfirmButtonClickListener listener;

    public void setOnConfirmButtonClickListener(OnConfirmButtonClickListener listener) {
        this.listener = listener;
    }
}
