package com.team5.taketac.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team5.taketac.ChatActivity;
import com.team5.taketac.R;
import com.team5.taketac.model.ChatRoomInfo;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {
    private final List<ChatRoomInfo> roomList;
    private final Context context;

    public ChatRoomAdapter(List<ChatRoomInfo> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chatroom, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoomInfo info = roomList.get(position);
        holder.textView.setText(TextUtils.join(", ", info.getNicknames()));

        // 🔹 채팅방 클릭 시 ChatActivity 실행
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatRoomId", info.getId());
            intent.putExtra("chatRoomName", TextUtils.join(", ", info.getNicknames()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public void setRooms(List<ChatRoomInfo> newRooms) {
        roomList.clear();
        roomList.addAll(newRooms);
        notifyDataSetChanged();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textRoomNames);
        }
    }
}
