package com.team5.taketac.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team5.taketac.R;
import com.team5.taketac.model.ChatRoomInfo;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoomInfo room);
    }

    private final List<ChatRoomInfo> roomList;
    private final Context context;
    private final OnChatRoomClickListener listener;

    public ChatRoomAdapter(List<ChatRoomInfo> roomList, Context context, OnChatRoomClickListener listener) {
        this.roomList = roomList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_public_party, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoomInfo info = roomList.get(position);
        String names = TextUtils.join(", ", info.getNicknames());

        holder.tvTitle.setText(names);
        holder.tvLocation.setText("채팅방");
        holder.tvTime.setText("");  // 시간 없으면 공백
        holder.tvParticipants.setText("참가자: " + info.getNicknames().size() + "명");
        holder.tvCreatorUid.setText("");  // 원한다면 " "로 둬도 됨

        holder.itemView.setOnClickListener(v -> listener.onChatRoomClick(info));
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
        TextView tvTitle, tvLocation, tvTime, tvParticipants, tvCreatorUid;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvCreatorUid = itemView.findViewById(R.id.tvCreatorUid);
        }
    }
}
