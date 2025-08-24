package com.example.smarttasksapp.infrastructure.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;
import com.example.smarttasksapp.infrastructure.database.AppDatabase;
import com.example.smarttasksapp.infrastructure.dao.TaskDao;
import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 任务仓库实现
 * 实现ITaskRepository接口，负责数据持久化
 * Feature层传入TaskEntity，转换为Infrastructure层的Task使用
 * 返回时将Task转换为TaskEntity
 */
public class TaskRepositoryImpl implements ITaskRepository {
    private static final String TAG = "TaskRepositoryImpl";
    
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    
    public TaskRepositoryImpl(Context context, ExecutorService executorService) {
        this.taskDao = AppDatabase.getInstance(context).taskDao();
        this.executorService = executorService;
        Log.d(TAG, "TaskRepositoryImpl initialized");
    }
    
    @Override
    public LiveData<List<TaskEntity>> observeAll() {
        // 使用Transformations.map将Infrastructure层的Task转换为Feature层的TaskEntity
        return Transformations.map(taskDao.observeAll(), this::convertToFeatureEntityList);
    }
    
    @Override
    public CompletableFuture<Long> addTask(String title, String description, long startTime) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 创建Infrastructure层的Task
                Task entity = new Task();
                entity.setTitle(title);
                entity.setDescription(description);
                entity.setStartTime(startTime);
                entity.setCompleted(false);
                entity.setCreatedAt(System.currentTimeMillis());
                entity.setSortIndex(0);
                
                long taskId = taskDao.insert(entity);
                Log.d(TAG, "Task added successfully with ID: " + taskId);
                return taskId;
            } catch (Exception e) {
                Log.e(TAG, "Error adding task: " + e.getMessage(), e);
                throw new RuntimeException("Failed to add task", e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> updateTask(TaskEntity taskEntity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 将Feature层的TaskEntity转换为Infrastructure层的Task
                Task task = convertToInfrastructureEntity(taskEntity);
                taskDao.update(task);
                Log.d(TAG, "Task updated successfully: " + task.getId());
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error updating task: " + e.getMessage(), e);
                throw new RuntimeException("Failed to update task", e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> deleteTask(long taskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                taskDao.deleteTask(taskId);
                Log.d(TAG, "Task deleted successfully: " + taskId);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error deleting task: " + e.getMessage(), e);
                throw new RuntimeException("Failed to delete task", e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> updateTaskCompletedStatus(long taskId, boolean isCompleted) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                taskDao.updateCompletedStatus(taskId, isCompleted);
                Log.d(TAG, "Task status updated: " + taskId + " -> " + isCompleted);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error updating task status: " + e.getMessage(), e);
                throw new RuntimeException("Failed to update task status", e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> updateTaskStartTime(long taskId, long startTime) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                taskDao.updateStartTime(taskId, startTime);
                Log.d(TAG, "Task start time updated: " + taskId + " -> " + startTime);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error updating task start time: " + e.getMessage(), e);
                throw new RuntimeException("Failed to update task start time", e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Boolean> persistOrder(List<TaskEntity> orderedTaskEntities) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 将Feature层的TaskEntity列表转换为Infrastructure层的Task列表
                List<Task> orderedTasks = orderedTaskEntities.stream()
                        .map(this::convertToInfrastructureEntity)
                        .collect(Collectors.toList());
                
                taskDao.updateSortIndices(orderedTasks);
                Log.d(TAG, "Task order persisted successfully");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error persisting task order: " + e.getMessage(), e);
                throw new RuntimeException("Failed to persist task order", e);
            }
        }, executorService);
    }
    
    /**
     * 将Feature层的TaskEntity转换为Infrastructure层的Task
     */
    private Task convertToInfrastructureEntity(TaskEntity taskEntity) {
        Task task = new Task();
        task.setId(taskEntity.getId());
        task.setTitle(taskEntity.getTitle());
        task.setDescription(taskEntity.getDescription());
        task.setStartTime(taskEntity.getStartTime());
        task.setCompleted(taskEntity.isCompleted());
        task.setCreatedAt(taskEntity.getCreatedAt());
        task.setSortIndex(taskEntity.getSortIndex());
        return task;
    }
    
    /**
     * 将Infrastructure层的Task转换为Feature层的TaskEntity
     */
    private TaskEntity convertToFeatureEntity(Task task) {
        return new TaskEntity(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getCreatedAt(),
            task.getSortIndex(),
            task.isCompleted(),
            task.getStartTime()
        );
    }
    
    /**
     * 将Infrastructure层的Task列表转换为Feature层的TaskEntity列表
     */
    private List<TaskEntity> convertToFeatureEntityList(List<Task> tasks) {
        if (tasks == null) return null;
        return tasks.stream()
                .map(this::convertToFeatureEntity)
                .collect(Collectors.toList());
    }
}
