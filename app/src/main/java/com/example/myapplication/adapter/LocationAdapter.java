package com.example.myapplication.adapter;

import static com.example.myapplication.util.ImageUtil.base64ToImage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.example.myapplication.activity.LocationActivity;
import com.example.myapplication.entity.Contact;
import com.example.myapplication.entity.Location;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter{
    private Context mContext;

    private RecyclerView mRecyclerView;

    private List<Location> mLocation;
    public LocationAdapter(Context context, RecyclerView recyclerView, List<Location> location) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mLocation = location;
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
        Location location=mLocation.get(position);
        MyVH myVH = (MyVH) holder;
        myVH.nicknameTextView.setText(location.getNickname());
        myVH.usernameTextView.setText(location.getUsername());

        String avatar=location.getAvatar();
        Bitmap avatarImage = base64ToImage(avatar);
        myVH.userAvatarImageView.setImageBitmap(avatarImage);
        myVH.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = location.getUsername();
                listener.onAddButtonClick(username);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLocation.size();
    }

    public static class MyVH extends RecyclerView.ViewHolder {
        public TextView nicknameTextView;
        public TextView usernameTextView;
        public ImageView userAvatarImageView;
        public Button addButton;

        public MyVH(@NonNull View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.nickname);
            usernameTextView = itemView.findViewById(R.id.username);
            addButton=itemView.findViewById(R.id.addButton);
            userAvatarImageView = itemView.findViewById(R.id.user_avatar);
        }
    }
    public interface OnAddButtonClickListener {
        void onAddButtonClick(String targetName);
    }
    private OnAddButtonClickListener listener;

    public void setOnConfirmButtonClickListener(OnAddButtonClickListener listener) {
        this.listener = listener;
    }
}
