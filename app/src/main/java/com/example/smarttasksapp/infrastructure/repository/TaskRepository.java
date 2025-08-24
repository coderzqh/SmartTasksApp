package com.example.smarttasksapp.infrastructure.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.feature.tasks.domain.Task;
import com.example.smarttasksapp.feature.tasks.domain.TaskMapper;
import com.example.smarttasksapp.infrastructure.database.AppDatabase;
import com.example.smarttasksapp.infrastructure.dao.TaskDao;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class TaskRepository implements ITaskRepository {
    private static final String TAG = "TaskRepository";
    private final TaskDao taskDao;
    private final ExecutorService executorService;

    public TaskRepository(Context context) {
        this.taskDao = AppDatabase.getInstance(context).taskDao();
        this.executorService = null; // 将由TaskModule管理
    }

    public TaskRepository(Context context, ExecutorService executorService) {
        this.taskDao = AppDatabase.getInstance(context).taskDao();
        this.executorService = executorService;
    }

    @Override
    public LiveData<List<Task>> observeAll() {
        // 使用Transformations.map将Infrastructure层的Task转换为Domain层的Task
        return Transformations.map(taskDao.observeAll(), TaskMapper::toDomainList);
    }

    @Override
    public long addTask(String title, String description, long startTime) {
        try {
            // 创建Domain层的Task
            Task domainTask = TaskMapper.createDomainTask(title, description, startTime);
            
            // 转换为Infrastructure层的Task
            com.example.smarttasksapp.infrastructure.entity.Task infrastructureTask = TaskMapper.toInfrastructure(domainTask);
            
            // 设置排序索引
            long max = taskDao.getMaxSortIndex();
            infrastructureTask.setSortIndex(max + 1);
            
            // 插入数据库并返回ID
            long taskId = taskDao.insert(infrastructureTask);
            Log.d(TAG, "Task added successfully: " + title + " with ID: " + taskId);
            return taskId;
        } catch (Exception e) {
            Log.e(TAG, "Error adding task: " + e.getMessage(), e);
            throw new RuntimeException("Failed to add task", e);
        }
    }

    @Override
    public void reorder(long fromTaskId, long toTaskId, boolean placeAbove) {
        if (executorService == null) {
            Log.e(TAG, "ExecutorService is null, cannot reorder tasks");
            return;
        }

        executorService.execute(() -> {
            try {
                // 简化：交换两个任务的 sortIndex；placeAbove 在此实现中不区分，直接交换
                long tmp = taskDao.getMaxSortIndex() + 1; // 临时占位，避免唯一性冲突
                taskDao.updateSortIndex(fromTaskId, tmp);
                // 读取 to 的 sortIndex 无 API，这里用两步：把 to 提前（max+2），from 设为原 to（max+1）
                // 简化策略：把目标放到队头，from 放到队头+1，达到视觉上的"移动"效果
                long maxNow = taskDao.getMaxSortIndex();
                taskDao.updateSortIndex(toTaskId, maxNow + 2);
                taskDao.updateSortIndex(fromTaskId, maxNow + 1);
                Log.d(TAG, "Tasks reordered successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error reordering tasks: " + e.getMessage(), e);
                throw new RuntimeException("Failed to reorder tasks", e);
            }
        });
    }

    @Override
    public void persistOrder(List<Task> ordered) {
        if (executorService == null) {
            Log.e(TAG, "ExecutorService is null, cannot persist order");
            return;
        }

        executorService.execute(() -> {
            try {
                // 将Domain层的Task列表转换为Infrastructure层的Task列表
                List<com.example.smarttasksapp.infrastructure.entity.Task> infrastructureTasks = TaskMapper.toInfrastructureList(ordered);
                
                // 最高在顶部：给顶部更大的 sortIndex，保证查询时位于前方
                long base = infrastructureTasks.size();
                for (int i = 0; i < infrastructureTasks.size(); i++) {
                    com.example.smarttasksapp.infrastructure.entity.Task task = infrastructureTasks.get(i);
                    long sort = base - i; // i 越小，sortIndex 越大
                    taskDao.updateSortIndex(task.getId(), sort);
                }
                Log.d(TAG, "Order persisted successfully for " + infrastructureTasks.size() + " tasks");
            } catch (Exception e) {
                Log.e(TAG, "Error persisting order: " + e.getMessage(), e);
                throw new RuntimeException("Failed to persist order", e);
            }
        });
    }

    @Override
    public void updateTask(long taskId, String title, String description, long startTime) {
        if (executorService == null) {
            Log.e(TAG, "ExecutorService is null, cannot update task");
            return;
        }

        executorService.execute(() -> {
            try {
                taskDao.updateTitleAndDescription(taskId, title, description);
                taskDao.updateStartTime(taskId, startTime);
                Log.d(TAG, "Task updated successfully: " + taskId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating task: " + e.getMessage(), e);
                throw new RuntimeException("Failed to update task", e);
            }
        });
    }
    
    @Override
    public void updateTaskCompletedStatus(long taskId, boolean isCompleted) {
        if (executorService == null) {
            Log.e(TAG, "ExecutorService is null, cannot update task status");
            return;
        }

        executorService.execute(() -> {
            try {
                taskDao.updateCompletedStatus(taskId, isCompleted);
                Log.d(TAG, "Task status updated successfully: " + taskId + " -> " + isCompleted);
            } catch (Exception e) {
                Log.e(TAG, "Error updating task status: " + e.getMessage(), e);
                throw new RuntimeException("Failed to update task status", e);
            }
        });
    }
    
    @Override
    public void updateTaskStartTime(long taskId, long startTime) {
        if (executorService == null) {
            Log.e(TAG, "ExecutorService is null, cannot update task start time");
            return;
        }

        executorService.execute(() -> {
            try {
                taskDao.updateStartTime(taskId, startTime);
                Log.d(TAG, "Task start time updated successfully: " + taskId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating task start time: " + e.getMessage(), e);
                throw new RuntimeException("Failed to update task start time", e);
            }
        });
    }
    
    @Override
    public void deleteTask(long taskId) {
        if (executorService == null) {
            Log.e(TAG, "ExecutorService is null, cannot delete task");
            return;
        }

        executorService.execute(() -> {
            try {
                taskDao.deleteTask(taskId);
                Log.d(TAG, "Task deleted successfully: " + taskId);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting task: " + e.getMessage(), e);
                throw new RuntimeException("Failed to delete task", e);
            }
        });
    }

    // 添加清理方法
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}


