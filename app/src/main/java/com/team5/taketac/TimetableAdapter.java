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

import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<DisplayableItem> displayableItems;
    private LayoutInflater inflater;
    private Context context;
    private final int SPAN_COUNT;

    public interface OnItemClickListener {
        void onItemClick(View view, ScheduleEntry entry);
    }

    private OnItemClickListener clickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

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

        switch (item.type) {
            case TIME_LABEL:
                holder.subjectNameTextView.setVisibility(View.VISIBLE);
                holder.classroomTextView.setVisibility(View.GONE);
                holder.subjectNameTextView.setText(item.text);
                holder.subjectNameTextView.setTextSize(12f);
                holder.itemView.setBackgroundColor(Color.LTGRAY);
                break;

            case SCHEDULE_ENTRY:
                if (item.originalEntry != null) {
                    holder.subjectNameTextView.setVisibility(View.VISIBLE);
                    holder.classroomTextView.setVisibility(View.VISIBLE);
                    holder.subjectNameTextView.setText(item.isContinuation ? "" : item.originalEntry.getSubjectName());
                    holder.classroomTextView.setText(item.isContinuation ? "" : item.originalEntry.getClassroom());
                }
                holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.timetable_item_background));
                break;

            case EMPTY_SLOT:
                holder.subjectNameTextView.setVisibility(View.INVISIBLE);
                holder.classroomTextView.setVisibility(View.INVISIBLE);
                holder.itemView.setBackgroundColor(Color.WHITE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return displayableItems.size();
    }

    public void updateData(List<DisplayableItem> newItems) {
        this.displayableItems.clear();
        this.displayableItems.addAll(newItems);
        notifyDataSetChanged();
        Log.d("CHECK", "어댑터 데이터 수: " + displayableItems.size());

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectNameTextView;
        TextView classroomTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectNameTextView = itemView.findViewById(R.id.textViewSubjectName);
            classroomTextView = itemView.findViewById(R.id.textViewClassroom);
        }
    }
}




