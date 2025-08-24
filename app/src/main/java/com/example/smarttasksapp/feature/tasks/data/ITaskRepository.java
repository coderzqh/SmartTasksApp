package com.example.smarttasksapp.feature.tasks.data;

import androidx.lifecycle.LiveData;

import com.example.smarttasksapp.feature.tasks.domain.Task;

import java.util.List;

public interface ITaskRepository {
    LiveData<List<Task>> observeAll();
    
    /**
     * 添加任务并返回任务ID
     * @param title 任务标题
     * @param description 任务描述
     * @param startTime 开始时间
     * @return 新创建的任务ID
     */
    long addTask(String title, String description, long startTime);
    
    void reorder(long fromTaskId, long toTaskId, boolean placeAbove);
    void persistOrder(List<Task> ordered);
    void updateTask(long taskId, String title, String description, long startTime);
    void updateTaskCompletedStatus(long taskId, boolean isCompleted);
    void updateTaskStartTime(long taskId, long startTime);
    void deleteTask(long taskId);
}
