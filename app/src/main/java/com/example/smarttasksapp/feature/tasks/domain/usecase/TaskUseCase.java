package com.example.smarttasksapp.feature.tasks.domain.usecase;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.smarttasksapp.core.util.CompletableFutureUtil;
import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;
import com.example.smarttasksapp.feature.tasks.domain.TaskReminderManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

/**
 * 任务用例
 * 封装任务相关的业务逻辑
 */
@ViewModelScoped
public class TaskUseCase {
    private final ITaskRepository repository;
    private final TaskReminderManager reminderManager;
    
    @Inject
    public TaskUseCase(ITaskRepository repository, TaskReminderManager reminderManager) {
        this.repository = repository;
        this.reminderManager = reminderManager;
    }
    
    public ITaskRepository getRepository() {
        return repository;
    }
    
    /**
     * 添加任务
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public CompletableFuture<Long> addTask(TaskEntity taskEntity) {
        // 业务逻辑验证
        if (taskEntity == null) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务实体不能为空"));
        }
        
        String title = taskEntity.getTitle();
        if (title == null || title.trim().isEmpty()) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务标题不能为空"));
        }
        
        if (title.length() > 200) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务标题过长，不能超过200个字符"));
        }
        
        long startTime = taskEntity.getStartTime();
        if (startTime <= 0) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("开始时间无效"));
        }
        
        return repository.addTask(title.trim(), taskEntity.getDescription(), startTime)
                .thenCompose(taskId -> {
                    // 设置任务ID并添加到提醒队列
                    taskEntity.setId(taskId);
                    reminderManager.addTaskToReminderQueue(taskEntity);
                    return CompletableFuture.completedFuture(taskId);
                });
    }
    
    /**
     * 更新任务
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public CompletableFuture<Boolean> updateTask(long taskId, String title, String description, long startTime) {
        // 业务逻辑验证
        if (taskId <= 0) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务ID无效"));
        }
        
        if (title == null || title.trim().isEmpty()) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务标题不能为空"));
        }
        
        if (title.length() > 200) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务标题过长，不能超过200个字符"));
        }
        
        if (startTime <= 0) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("开始时间无效"));
        }
        
        TaskEntity task = new TaskEntity(title.trim(), description, startTime);
        task.setId(taskId);
        
        return repository.updateTask(task)
                .thenApply(success -> {
                    if (success) {
                        reminderManager.updateTaskReminder(task);
                    }
                    return success;
                });
    }
    
    /**
     * 删除任务
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public CompletableFuture<Boolean> deleteTask(long taskId) {
        if (taskId <= 0) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务ID无效"));
        }
        
        return repository.deleteTask(taskId)
                .thenApply(success -> {
                    if (success) {
                        reminderManager.removeTaskFromReminderQueue(taskId);
                    }
                    return success;
                });
    }
    
    /**
     * 更新任务状态
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public CompletableFuture<Boolean> updateTaskStatus(long taskId, boolean isCompleted) {
        if (taskId <= 0) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务ID无效"));
        }
        
        return repository.updateTaskCompletedStatus(taskId, isCompleted);
    }
    
    /**
     * 更新任务开始时间
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public CompletableFuture<Boolean> updateTaskStartTime(long taskId, long startTime) {
        if (taskId <= 0) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务ID无效"));
        }
        
        if (startTime <= 0) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("开始时间无效"));
        }
        
        return repository.updateTaskStartTime(taskId, startTime)
                .thenApply(success -> {
                    if (success) {
                        // 获取任务信息并更新提醒
                        // 这里简化处理，实际应用中可能需要从仓库获取完整任务信息
                        TaskEntity task = new TaskEntity();
                        task.setId(taskId);
                        task.setStartTime(startTime);
                        reminderManager.updateTaskReminder(task);
                    }
                    return success;
                });
    }
    
    /**
     * 持久化任务顺序
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public CompletableFuture<Boolean> persistTaskOrder(List<TaskEntity> orderedTasks) {
        if (orderedTasks == null || orderedTasks.isEmpty()) {
            return CompletableFutureUtil.failedFuture(new IllegalArgumentException("任务列表不能为空"));
        }
        
        return repository.persistOrder(orderedTasks);
    }
    
    /**
     * 获取任务统计信息
     */
    public TaskStatistics getTaskStatistics(List<TaskEntity> tasks) {
        if (tasks == null) {
            return new TaskStatistics(0, 0, 0, 0.0);
        }
        
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(TaskEntity::isCompleted).count();
        int pending = total - completed;
        double completionRate = total > 0 ? (double) completed / total * 100 : 0.0;
        
        return new TaskStatistics(total, completed, pending, completionRate);
    }
    
    /**
     * 任务统计信息
     */
    public static class TaskStatistics {
        private final int totalTasks;
        private final int completedTasks;
        private final int pendingTasks;
        private final double completionRate;
        
        public TaskStatistics(int totalTasks, int completedTasks, int pendingTasks, double completionRate) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
            this.completionRate = completionRate;
        }
        
        public int getTotalTasks() { return totalTasks; }
        public int getCompletedTasks() { return completedTasks; }
        public int getPendingTasks() { return pendingTasks; }
        public double getCompletionRate() { return completionRate; }
    }
}
