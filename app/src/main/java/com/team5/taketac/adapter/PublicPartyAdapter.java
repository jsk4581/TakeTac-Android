package com.team5.taketac.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team5.taketac.PartyDetailBottomSheetFragment;
import com.team5.taketac.R;
import com.team5.taketac.model.PartyRoom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PublicPartyAdapter extends RecyclerView.Adapter<PublicPartyAdapter.PartyViewHolder> {

    private List<PartyRoom> partyList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public PublicPartyAdapter(List<PartyRoom> partyList) {
        this.partyList = partyList;
    }

    @NonNull
    @Override
    public PartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_public_party, parent, false);
        return new PartyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartyViewHolder holder, int position) {
        PartyRoom party = partyList.get(position);

        holder.tvTitle.setText(party.getTitle());
        holder.tvLocation.setText("장소: " + party.getLocation());
        holder.tvTime.setText("시간: " + formatTimestamp(party.getTimestamp()));
        holder.tvCreatorUid.setText("생성자: " + party.getCreatorUid());

        // ✅ 참가자 수는 Firebase에서 직접 조회
        holder.tvParticipants.setText("참가자: 불러오는 중...");
        FirebaseDatabase.getInstance().getReference("partyRooms")
                .child(party.getId())
                .child("participants")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = (int) snapshot.getChildrenCount();
                        holder.tvParticipants.setText("참가자: " + count + "명");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.tvParticipants.setText("참가자 수 불러오기 실패");
                    }
                });

        // 바텀시트 열기
        holder.itemView.setOnClickListener(v -> {
            PartyDetailBottomSheetFragment fragment = new PartyDetailBottomSheetFragment(party);
            fragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "PartyDetail");
        });
    }

    @Override
    public int getItemCount() {
        return partyList.size();
    }

    public static class PartyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvTime, tvParticipants, tvCreatorUid;

        public PartyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvCreatorUid = itemView.findViewById(R.id.tvCreatorUid);
        }
    }

    private String formatTimestamp(long millis) {
        return dateFormat.format(new Date(millis));
    }
}
