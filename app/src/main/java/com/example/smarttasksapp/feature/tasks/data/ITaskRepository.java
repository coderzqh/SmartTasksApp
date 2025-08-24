package com.example.smarttasksapp.feature.tasks.data;

import androidx.lifecycle.LiveData;

import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ITaskRepository {
    LiveData<List<TaskEntity>> observeAll();
    
    /**
     * 添加任务并返回任务ID
     * @param title 任务标题
     * @param description 任务描述
     * @param startTime 开始时间
     * @return 新创建的任务ID
     */
    CompletableFuture<Long> addTask(String title, String description, long startTime);

    CompletableFuture<Boolean> persistOrder(List<TaskEntity> ordered);
    CompletableFuture<Boolean> updateTask(TaskEntity task);
    CompletableFuture<Boolean> updateTaskCompletedStatus(long taskId, boolean isCompleted);
    CompletableFuture<Boolean> updateTaskStartTime(long taskId, long startTime);
    CompletableFuture<Boolean> deleteTask(long taskId);
}
