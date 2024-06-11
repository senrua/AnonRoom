package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.AnonGroupActivity;
import com.example.myapplication.activity.PrivateActivity;
import com.example.myapplication.activity.WeatherActivity;
import com.example.myapplication.entity.Room;
import com.example.myapplication.util.DPIUtil;
import com.example.myapplication.view.LeftSlideView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;


public class RoomAdapter extends RecyclerView.Adapter {

    private Context mContext;

    private RecyclerView mRecyclerView;

    private LeftSlideView mLeftSlideView;
    private List<Room> mRoom;

    public RoomAdapter(Context context, RecyclerView recyclerView, List<Room> rooms) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        this.mRoom = rooms;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final LeftSlideView leftSlideView = new LeftSlideView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT
                , DPIUtil.dip2px(100.f));

        leftSlideView.setLayoutParams(params);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contentView = inflater.inflate(R.layout.layout_content, null);
        View menuView = inflater.inflate(R.layout.layout_menu, null);

        leftSlideView.addContentView(contentView);
        leftSlideView.addMenuView(menuView);
        leftSlideView.setRecyclerView(mRecyclerView);
        leftSlideView.setStatusChangeLister(new LeftSlideView.OnDelViewStatusChangeLister() {
            @Override
            public void onStatusChange(boolean show) {
                if (show) {
                    // 如果编辑菜单在显示
                    mLeftSlideView = leftSlideView;
                }
            }
        });


        return new MyVH(leftSlideView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Room room = mRoom.get(position);
        MyVH myVH = (MyVH) holder;
        myVH.nameTextView.setText(room.getName());
        if(room.getRoomType()==0) {
            String avatarImagePath = room.getAvatarPath();
            if (avatarImagePath != null) {
                Picasso.get().load(new File(avatarImagePath)).into(myVH.userAvatarImageView);
            }
        }
        else{
            Picasso.get().load(R.drawable.anon_group).into(myVH.userAvatarImageView);
        }
        myVH.itemView.findViewById(R.id.layout_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取当前item的位置
                int position = holder.getBindingAdapterPosition();
                // 移除该位置的item
                removeItem(position);
            }
        });

        // 为contentView设置OnClickListener
        myVH.itemView.findViewById(R.id.layout_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取当前item的位置
                int position = holder.getBindingAdapterPosition();
                Room room = mRoom.get(position);
                int roomType = room.getRoomType();

                Intent intent;
                switch (roomType) {
                    case 0:
                        // 如果房间类型为0，跳转到PrivateActivity
                        intent = new Intent(mContext, PrivateActivity.class);
                        break;
                    case 1:
                        // 如果房间类型为1，跳转到AnonGroupActivity
                        intent = new Intent(mContext, AnonGroupActivity.class);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown room type: " + roomType);
                }

                // 启动Activity
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return  mRoom.size();
    }

    /**
     * 还原itemView
     * @param point
     */
    public void restoreItemView(Point point) {
        if (mLeftSlideView != null) {

            int[] pos = new int[2];


            mLeftSlideView.getLocationInWindow(pos);

            int width = mLeftSlideView.getWidth();
            int height = mLeftSlideView.getHeight();

            // 触摸点在view的区域内，那么直接返回
            if (point.x >= pos[0] && point.y >= pos[1]
                    && point.x <= pos[0] + width && point.y <= pos[1] + height) {

                return;
            }

            mLeftSlideView.resetDelStatus();
        }
    }
    public static class MyVH extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public ImageView userAvatarImageView;
        public LeftSlideView leftSlideView; // 添加 LeftSlideView 的引用

        public MyVH(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            userAvatarImageView = itemView.findViewById(R.id.user_avatar);
            leftSlideView = (LeftSlideView) itemView; // 初始化 LeftSlideView 的引用
        }
    }
    public void removeItem(int position) {
        mRoom.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mRoom.size());
    }
}
