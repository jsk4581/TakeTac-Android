package com.team5.taketac.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team5.taketac.R;
import com.team5.taketac.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messages;
    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textView.setText(message.getText());

        // 닉네임 스타일 적용
        holder.nicknameText.setVisibility(View.VISIBLE);
        holder.nicknameText.setText(message.getSenderNickname());
        holder.nicknameText.setTextColor(Color.BLACK); // 검정색
        holder.nicknameText.setTypeface(null, Typeface.BOLD); // 볼드체

        if (message.isSentByUser()) {
            // 내 메시지: 오른쪽 정렬
            holder.textView.setBackgroundResource(R.drawable.bg_sent);
            holder.messageContainer.setGravity(Gravity.END);
            holder.textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            // 상대 메시지: 왼쪽 정렬
            holder.textView.setBackgroundResource(R.drawable.bg_received);
            holder.messageContainer.setGravity(Gravity.START);
            holder.textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        TextView textView;
        TextView nicknameText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            textView = itemView.findViewById(R.id.messageText);
            nicknameText = itemView.findViewById(R.id.nicknameText);
        }
    }
}
