package com.example.smarttasksapp.feature.tasks.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.example.smarttasksapp.R;
import com.example.smarttasksapp.feature.tasks.ui.adapter.TaskAdapter;
import com.example.smarttasksapp.feature.tasks.ui.adapter.SwipeToCompleteCallback;
import com.example.smarttasksapp.feature.tasks.ui.view.AddTaskBottomSheet;
import com.example.smarttasksapp.feature.tasks.ui.view.TaskDetailBottomSheet;
import com.example.smarttasksapp.feature.tasks.ui.viewmodel.TaskViewModel;
import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.smarttasksapp.infrastructure.entity.Task;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksFragment extends Fragment {

    private static final String TAG = "TasksFragment";
    private TaskViewModel viewModel;
    private TaskAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);

        // 初始化RecyclerView
        RecyclerView recyclerView = rootView.findViewById(R.id.rvTasks);
        adapter = new TaskAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 初始化SwipeToCompleteCallback，实现左滑完成和长按拖拽排序
        SwipeToCompleteCallback swipeToCompleteCallback = new SwipeToCompleteCallback(adapter, (taskId, isCompleted) -> {
            viewModel.updateTaskStatus(taskId, isCompleted);
        });

        // 设置任务拖拽排序监听器
        swipeToCompleteCallback.setOnMoveListener((from, to) -> {
            // 交换列表中的任务位置
            List<TaskEntity> tasks = adapter.getCurrentList();
            TaskEntity movedTask = tasks.get(from);
            List<TaskEntity> newTasks = new ArrayList<>(tasks);
            newTasks.remove(from);
            newTasks.add(to, movedTask);
            adapter.submitList(newTasks);
            return true;
        });

        // 设置拖拽完成监听器，更新排序索引
        swipeToCompleteCallback.setOnDragCompleteListener(orderedTasks -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                viewModel.persistTaskOrder(new ArrayList<>(orderedTasks));
            }
        });

        // 将ItemTouchHelper与RecyclerView关联
        new ItemTouchHelper(swipeToCompleteCallback).attachToRecyclerView(recyclerView);

        // 设置任务点击监听器，用于显示任务详情
        adapter.setOnTaskClickListener(task -> {
            TaskDetailBottomSheet.newInstance(task)
                    .show(getChildFragmentManager(), "taskDetail");
        });

        // 观察任务列表变化
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                adapter.submitList(tasks);
                // 更新空状态提示
                rootView.findViewById(R.id.tv_empty_hint).setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        // 添加任务按钮点击事件
        rootView.findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "addTask");
        });

        return rootView;
    }
}