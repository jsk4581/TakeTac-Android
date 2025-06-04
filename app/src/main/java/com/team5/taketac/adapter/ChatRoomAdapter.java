package com.team5.taketac.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team5.taketac.R;
import com.team5.taketac.model.ChatRoom;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(ChatRoom room);     // 전체 아이템 클릭
        void onJoinClick(ChatRoom room);     // 참여 버튼 클릭
        void onDeleteClick(ChatRoom room);  // 삭제 버튼 클릭
    }

    private final List<ChatRoom> chatRooms;
    private final OnRoomClickListener listener;

    public ChatRoomAdapter(List<ChatRoom> chatRooms, OnRoomClickListener listener) {
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);
        holder.roomName.setText(room.getName());

        // 전체 아이템 클릭 시
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoomClick(room);
            }
        });

        // 참여 버튼 클릭 시
        holder.btnJoinRoom.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJoinClick(room);
            }
        });

        // 삭제 버튼 클릭 시
        holder.btnDeleteRoom.setVisibility(View.VISIBLE);
        holder.btnDeleteRoom.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        Button btnJoinRoom;
        Button btnDeleteRoom; // 삭제 버튼 추가

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
            btnJoinRoom = itemView.findViewById(R.id.btnJoinRoom);
            btnDeleteRoom = itemView.findViewById(R.id.btnDeleteRoom);
        }
    }
}
package com.team5.taketac.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team5.taketac.R;
import com.team5.taketac.model.ChatRoom;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(ChatRoom room);     // 전체 아이템 클릭
        void onJoinClick(ChatRoom room);     // 참여 버튼 클릭
        void onDeleteClick(ChatRoom room);  // 삭제 버튼 클릭
    }

    private final List<ChatRoom> chatRooms;
    private final OnRoomClickListener listener;

    public ChatRoomAdapter(List<ChatRoom> chatRooms, OnRoomClickListener listener) {
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);
        holder.roomName.setText(room.getName());

        // 전체 아이템 클릭 시
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoomClick(room);
            }
        });

        // 참여 버튼 클릭 시
        holder.btnJoinRoom.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJoinClick(room);
            }
        });

        // 삭제 버튼 클릭 시
        holder.btnDeleteRoom.setVisibility(View.VISIBLE);
        holder.btnDeleteRoom.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        Button btnJoinRoom;
        Button btnDeleteRoom; // 삭제 버튼 추가

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
            btnJoinRoom = itemView.findViewById(R.id.btnJoinRoom);
            btnDeleteRoom = itemView.findViewById(R.id.btnDeleteRoom);
        }
    }
}
