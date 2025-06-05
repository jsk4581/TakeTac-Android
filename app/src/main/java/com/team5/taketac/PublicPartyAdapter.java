package com.team5.taketac;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PublicPartyAdapter extends RecyclerView.Adapter<PublicPartyAdapter.PartyViewHolder> {

    private Context context;
    private List<PublicParty> partyList;

    public PublicPartyAdapter(Context context, List<PublicParty> partyList) {
        this.context = context;
        this.partyList = partyList;
    }

    public void setPartyList(List<PublicParty> list) {
        this.partyList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_public_party, parent, false);
        return new PartyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartyViewHolder holder, int position) {
        PublicParty party = partyList.get(position);
        holder.tvTitle.setText(party.getTitle());
        holder.tvLocation.setText("장소: " + party.getLocation());

        // timestamp → 시간 포맷팅
        if (party.getTimestamp() != 0L) {
            String formattedTime = formatTimestamp(party.getTimestamp());
            holder.tvTime.setText("시간: " + formattedTime);
        } else {
            holder.tvTime.setText("시간: -");
        }

        // 아이템 클릭 → 상세 BottomSheet
        holder.itemView.setOnClickListener(v -> {
            PartyDetailBottomSheetFragment dialog = new PartyDetailBottomSheetFragment(party);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "PartyDetail");
        });
    }

    @Override
    public int getItemCount() {
        return partyList != null ? partyList.size() : 0;
    }

    public static class PartyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvTime;

        public PartyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
