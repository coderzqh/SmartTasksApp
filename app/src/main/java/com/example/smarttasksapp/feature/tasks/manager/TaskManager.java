package com.example.smarttasksapp.feature.tasks.manager;

import android.content.Context;
import android.util.Log;

import com.example.smarttasksapp.core.AppInitializer;
import com.example.smarttasksapp.core.manager.BaseManager;
import com.example.smarttasksapp.feature.tasks.constants.TaskConstants;
import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.feature.tasks.domain.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 任务管理器
 * 继承BaseManager，统一管理任务相关的业务逻辑
 */
public class TaskManager extends BaseManager {
    private static final String MODULE_NAME = "Tasks";
    private final ITaskRepository repository;

    public TaskManager(Context context) {
        super(context, MODULE_NAME);
        System.out.println("TaskManager: Attempting to get ITaskRepository from AppModule");
        this.repository = appModule.getSingleton(ITaskRepository.class);
        if (this.repository == null) {
            // 如果ITaskRepository没有注册，尝试重新初始化
            logWarning("ITaskRepository not found in AppModule, attempting to reinitialize");
            System.out.println("TaskManager: ITaskRepository not found, reinitializing AppInitializer");
            AppInitializer.initialize(context);
            if (this.repository == null) {
                throw new IllegalStateException("ITaskRepository not registered in AppModule after reinitialization");
            }
        }
        System.out.println("TaskManager: ITaskRepository obtained successfully");
    }

    /**
     * 添加任务并返回任务ID
     */
    public CompletableFuture<Long> addTaskAsync(String title, String description, long startTime) {
        return executeAsyncWithEvent(() -> {
            validateTaskData(title);
            long taskId = repository.addTask(title, description, startTime);
            logDebug("Task added successfully: " + title + " with ID: " + taskId);
            return taskId;
        }, "addTask");
    }

    /**
     * 更新任务
     */
    public CompletableFuture<Boolean> updateTaskAsync(long taskId, String title, String description, long startTime) {
        return executeAsyncWithEvent(() -> {
            validateTaskData(title);
            validatePositive(taskId, "taskId");
            repository.updateTask(taskId, title, description, startTime);
            logDebug("Task updated successfully: " + taskId);
            return true;
        }, "updateTask");
    }

    /**
     * 删除任务
     */
    public CompletableFuture<Boolean> deleteTaskAsync(long taskId) {
        return executeAsyncWithEvent(() -> {
            validatePositive(taskId, "taskId");
            repository.deleteTask(taskId);
            logDebug("Task deleted successfully: " + taskId);
            return true;
        }, "deleteTask");
    }

    /**
     * 更新任务完成状态
     */
    public CompletableFuture<Boolean> updateTaskStatusAsync(long taskId, boolean isCompleted) {
        return executeAsyncWithEvent(() -> {
            validatePositive(taskId, "taskId");
            repository.updateTaskCompletedStatus(taskId, isCompleted);
            logDebug("Task status updated: " + taskId + " -> " + isCompleted);
            return true;
        }, "updateTaskStatus");
    }

    /**
     * 批量更新任务状态
     */
    public CompletableFuture<Boolean> batchUpdateTaskStatusAsync(List<Long> taskIds, boolean isCompleted) {
        return executeAsyncWithEvent(() -> {
            validateNotNull(taskIds, "taskIds");
            for (Long taskId : taskIds) {
                validatePositive(taskId, "taskId");
                repository.updateTaskCompletedStatus(taskId, isCompleted);
            }
            logDebug("Batch task status updated: " + taskIds.size() + " tasks -> " + isCompleted);
            return true;
        }, "batchUpdateTaskStatus");
    }

    /**
     * 重新排序任务
     */
    public CompletableFuture<Boolean> reorderTasksAsync(long fromTaskId, long toTaskId, boolean placeAbove) {
        return executeAsyncWithEvent(() -> {
            validatePositive(fromTaskId, "fromTaskId");
            validatePositive(toTaskId, "toTaskId");
            repository.reorder(fromTaskId, toTaskId, placeAbove);
            logDebug("Tasks reordered successfully");
            return true;
        }, "reorderTasks");
    }

    /**
     * 持久化任务顺序
     */
    public CompletableFuture<Boolean> persistTaskOrderAsync(List<Task> orderedTasks) {
        return executeAsyncWithEvent(() -> {
            validateNotNull(orderedTasks, "orderedTasks");
            repository.persistOrder(orderedTasks);
            logDebug("Task order persisted successfully");
            return true;
        }, "persistTaskOrder");
    }

    /**
     * 获取任务统计信息
     */
    public CompletableFuture<TaskStatistics> getTaskStatisticsAsync(List<Task> tasks) {
        return executeAsync(() -> {
            validateNotNull(tasks, "tasks");
            int total = tasks.size();
            int completed = (int) tasks.stream().filter(Task::isCompleted).count();
            int pending = total - completed;
            double completionRate = total > 0 ? (double) completed / total * 100 : 0.0;

            return new TaskStatistics(total, completed, pending, completionRate);
        });
    }

    /**
     * 验证任务数据
     */
    private void validateTaskData(String title) {
        validateNotEmpty(title, "title");
        if (title.length() > 200) {
            throw new IllegalArgumentException("任务标题过长，不能超过200个字符");
        }
    }

    /**
     * 任务统计信息类
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
