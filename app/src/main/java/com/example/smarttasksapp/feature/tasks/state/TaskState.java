package com.example.smarttasksapp.feature.tasks.state;

import androidx.lifecycle.MutableLiveData;

import com.example.smarttasksapp.core.state.BaseState;
import com.example.smarttasksapp.feature.tasks.domain.Task;

import java.util.List;

/**
 * 任务状态管理类
 * 继承BaseState，统一管理任务相关的状态数据
 */
public class TaskState extends BaseState {
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Task> selectedTask = new MutableLiveData<>();

    // 任务列表状态
    public MutableLiveData<List<Task>> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> taskList) {
        tasks.setValue(taskList);
    }

    // 选中的任务
    public MutableLiveData<Task> getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(Task task) {
        selectedTask.setValue(task);
    }

    @Override
    protected void onReset() {
        setSelectedTask(null);
        setTasks(null);
    }

    // 获取任务统计信息
    public int getTotalTaskCount() {
        List<Task> currentTasks = tasks.getValue();
        return currentTasks != null ? currentTasks.size() : 0;
    }

    public int getCompletedTaskCount() {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks == null) return 0;
        
        return (int) currentTasks.stream()
                .filter(Task::isCompleted)
                .count();
    }

    public int getPendingTaskCount() {
        return getTotalTaskCount() - getCompletedTaskCount();
    }

    public double getCompletionRate() {
        int total = getTotalTaskCount();
        if (total == 0) return 0.0;
        return (double) getCompletedTaskCount() / total * 100;
    }
}
