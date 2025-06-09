package com.team5.taketac.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private List<String> nicknameList;

    public ParticipantAdapter(List<String> nicknameList) {
        this.nicknameList = nicknameList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.nicknameText.setText(nicknameList.get(position));
    }

    @Override
    public int getItemCount() {
        return nicknameList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nicknameText = itemView.findViewById(android.R.id.text1);
        }
    }
}
