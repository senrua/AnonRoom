package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.FriendProfileActivity;
import com.example.myapplication.entity.Contact;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter{
    private Context mContext;

    private RecyclerView mRecyclerView;

    private List<Contact> mContact;
    public ContactAdapter(Context context, RecyclerView recyclerView, List<Contact> contact) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mContact = contact;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactLinearLayout= inflater.inflate(R.layout.layout_contact, null);
        return new MyVH(contactLinearLayout);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Contact contact = mContact.get(position);
        ContactAdapter.MyVH myVH = (ContactAdapter.MyVH) holder;
        myVH.nicknameTextView.setText(contact.getNickname());
        myVH.usernameTextView.setText(contact.getUsername());

        String avatarImagePath=contact.getAvatarPath();
        if (avatarImagePath != null) {
            Picasso.get().load(new File(avatarImagePath)).into(myVH.userAvatarImageView);
        }

        myVH.contactLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳转至 FriendProfileActivity
                Intent intent = new Intent(mContext, FriendProfileActivity.class);
                // 将好友的 username 作为 "username" 的键值对添加到 Intent 中
                intent.putExtra("username", contact.getUsername());
                // 启动 FriendProfileActivity
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContact.size();
    }

    public static class MyVH extends RecyclerView.ViewHolder {
        public TextView nicknameTextView;
        public TextView usernameTextView;
        public ImageView userAvatarImageView;
        public LinearLayout contactLinearLayout;

        public MyVH(@NonNull View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.nickname);
            usernameTextView = itemView.findViewById(R.id.username);
            userAvatarImageView = itemView.findViewById(R.id.user_avatar);
            contactLinearLayout = (LinearLayout) itemView;
        }
    }
}
