package com.example.smarttasksapp.feature.tasks.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttasksapp.R;
import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;
import com.example.smarttasksapp.feature.tasks.ui.view.TaskDetailBottomSheet;
import com.example.smarttasksapp.core.constants.Constants;
import com.google.android.material.card.MaterialCardView;

import java.util.Objects;

public class TaskAdapter extends ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder> {

    private OnTaskStatusChangeListener statusChangeListener;
    private OnTaskClickListener taskClickListener;

    public interface OnTaskStatusChangeListener {
        void onTaskStatusChanged(long taskId, boolean isCompleted);
    }

    public interface OnTaskClickListener {
        void onTaskClick(TaskEntity task);
    }

    public TaskAdapter() {
        super(DIFF);
    }

    public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.taskClickListener = listener;
    }

    private static final DiffUtil.ItemCallback<TaskEntity> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            // 优化比较逻辑，只比较关键字段
            return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                   Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                   oldItem.isCompleted() == newItem.isCompleted() &&
                   oldItem.getStartTime() == newItem.getStartTime();
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = getItem(position);
        holder.bind(task);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView taskCard;
        final TextView title;
        final TextView desc;
        final LinearLayout leftSwipeBackground;
        final ImageView completeIcon;
        final TextView completeText;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCard = itemView.findViewById(R.id.taskCard);
            title = itemView.findViewById(R.id.tvTitle);
            desc = itemView.findViewById(R.id.tvDesc);
            leftSwipeBackground = itemView.findViewById(R.id.leftSwipeBackground);
            completeIcon = itemView.findViewById(R.id.ivCompleteIcon);
            completeText = itemView.findViewById(R.id.tvCompleteText);
        }

        void bind(TaskEntity task) {
            // 设置任务内容
            title.setText(task.getTitle());
            desc.setText(task.getDescription() == null ? "" : task.getDescription());
            
            // 根据完成状态设置视觉样式
            updateVisualStyle(task.isCompleted());

            // 设置点击事件
            taskCard.setOnClickListener(v -> {
                if (taskClickListener != null) {
                    taskClickListener.onTaskClick(task);
                } else {
                    // 默认行为：显示详情
                    TaskDetailBottomSheet.newInstance(task)
                            .show(((androidx.fragment.app.FragmentActivity) v.getContext()).getSupportFragmentManager(), "taskDetail");
                }
            });
        }

        private void updateVisualStyle(boolean isCompleted) {
            if (isCompleted) {
                // 已完成：灰色样式
                taskCard.setCardBackgroundColor(Color.LTGRAY);
                title.setTextColor(Color.DKGRAY);
                desc.setTextColor(Color.GRAY);
                completeIcon.setImageResource(android.R.drawable.ic_menu_revert);
                completeText.setText(Constants.INCOMPLETE);
                leftSwipeBackground.setBackgroundColor(Color.parseColor(Constants.COLOR_COMPLETED)); // 橙色
            } else {
                // 未完成：正常样式
                taskCard.setCardBackgroundColor(Color.WHITE);
                title.setTextColor(Color.BLACK);
                desc.setTextColor(Color.DKGRAY);
                completeIcon.setImageResource(android.R.drawable.ic_menu_send);
                completeText.setText(Constants.COMPLETE);
                leftSwipeBackground.setBackgroundColor(Color.parseColor(Constants.COLOR_PENDING)); // 绿色
            }
        }
    }
}


