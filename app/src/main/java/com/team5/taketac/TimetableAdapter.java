package com.team5.taketac;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<DisplayableItem> displayableItems;
    private LayoutInflater inflater;
    private Context context;
    private final int SPAN_COUNT; // 예: 6 (시간 + 월화수목금)

    public TimetableAdapter(Context context, List<DisplayableItem> displayableItems, int spanCount) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.displayableItems = displayableItems;
        this.SPAN_COUNT = spanCount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.timetable_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayableItem item = displayableItems.get(position);
        Log.d("AdapterBind", "Binding item at position: " + position + ", type=" + item.type);



        switch (item.type) {
            case TIME_LABEL:
                holder.subjectNameTextView.setText(item.text);
                holder.subjectNameTextView.setTextSize(12f); // 시간 폰트 크기
                holder.classroomTextView.setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(Color.LTGRAY); // 시간 레이블 배경색
                break;
            case SCHEDULE_ENTRY:
                if (item.originalEntry != null) {
                    holder.subjectNameTextView.setText("");
                    holder.classroomTextView.setText("");
                    holder.subjectNameTextView.setVisibility(View.VISIBLE);
                    holder.classroomTextView.setVisibility(View.VISIBLE);

                    if (item.isContinuation) {
                        holder.subjectNameTextView.setVisibility(View.INVISIBLE);
                        holder.classroomTextView.setVisibility(View.INVISIBLE);
                    } else {
                        holder.subjectNameTextView.setText(item.originalEntry.getSubjectName());
                        holder.classroomTextView.setText(item.originalEntry.getClassroom());
                    }

                    holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.timetable_item_background));
                }
                break;
            case EMPTY_SLOT:
                holder.subjectNameTextView.setText("");
                holder.classroomTextView.setText("");
                holder.itemView.setBackgroundColor(Color.WHITE); // 빈 슬롯 배경색
                break;
        }
    }

    @Override
    public int getItemCount() {
        return displayableItems.size();
    }

    public void updateData(List<DisplayableItem> newItems) {
        this.displayableItems = new ArrayList<>(newItems); // 복사본으로 대체
        notifyDataSetChanged();
    }



    // ViewHolder 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectNameTextView;
        TextView classroomTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectNameTextView = itemView.findViewById(R.id.textViewSubjectName);
            classroomTextView = itemView.findViewById(R.id.textViewClassroom);
        }
    }

    // (선택 사항) 과목별로 다른 색상을 주기 위한 간단한 해시 기반 색상 생성기
    private int getRandomColor(String subjectName) {
        int hash = subjectName.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        // 너무 어둡거나 밝지 않게 조정
        r = Math.min(200, Math.max(100, r));
        g = Math.min(200, Math.max(100, g));
        b = Math.min(200, Math.max(100, b));
        return Color.rgb(r, g, b);
    }
}
