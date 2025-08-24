package com.example.smarttasksapp.feature.tasks.ui.viewmodel;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttasksapp.core.lifecycle.AppLifecycleManager;
import com.example.smarttasksapp.core.lifecycle.LifecycleScope;
import com.example.smarttasksapp.feature.tasks.constants.TaskConstants;
import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;
import com.example.smarttasksapp.feature.tasks.domain.usecase.TaskUseCase;

import java.util.List;

/**
 * 任务视图模型
 * 使用新的架构，直接管理状态，通过TaskUseCase处理业务逻辑
 */
public class TaskViewModel extends AndroidViewModel {
    private static final String TAG = "TaskViewModel";
    
    // 状态管理
    private final MutableLiveData<List<TaskEntity>> tasks = new MutableLiveData<>();
    private final MutableLiveData<TaskEntity> selectedTask = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOperationSuccessful = new MutableLiveData<>(false);
    
    // 业务逻辑
    private final TaskUseCase taskUseCase;
    private final LifecycleScope scope;
    
    public TaskViewModel(@NonNull Application application) {
        super(application);
        
        // 通过生命周期管理器获取依赖
        AppLifecycleManager lifecycleManager = AppLifecycleManager.getInstance();
        
        // 创建ViewModel作用域
        this.scope = lifecycleManager.createScope("TaskViewModel_" + System.currentTimeMillis());
        
        // 获取依赖
        ITaskRepository repository = lifecycleManager.getDependency(ITaskRepository.class);
        this.taskUseCase = new TaskUseCase(repository);
        
        // 观察任务列表
        observeTasks();
        
        Log.d(TAG, "TaskViewModel initialized successfully");
    }
    
    // 任务数据访问方法
    public LiveData<List<TaskEntity>> getTasks() {
        return tasks;
    }
    
    public LiveData<TaskEntity> getSelectedTask() {
        return selectedTask;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<Boolean> getIsOperationSuccessful() {
        return isOperationSuccessful;
    }
    
    // 任务操作方法
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void addTask(String title, String description, long startTime) {
        clearError();
        setLoading(true);
        
        Log.d(TAG, "Adding task: " + title);
        
        // 创建TaskEntity实例
        TaskEntity taskEntity = new TaskEntity(title, description, startTime);
        
        taskUseCase.addTask(taskEntity)
                .thenAccept(taskId -> {
                    if (taskId > 0) {
                        setOperationSuccessful(true);
                        Log.d(TAG, "Task added successfully with ID: " + taskId);
                    } else {
                        String errorMsg = "添加任务失败：无法获取任务ID";
                        setError(errorMsg);
                        Log.e(TAG, "Failed to add task: " + errorMsg);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = "添加任务失败：" + throwable.getMessage();
                    setError(errorMsg);
                    Log.e(TAG, "Error adding task: " + throwable.getMessage(), throwable);
                    return null;
                })
                .whenComplete((result, throwable) -> setLoading(false));
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void updateTask(long taskId, String title, String description, long startTime) {
        clearError();
        setLoading(true);
        
        Log.d(TAG, "Updating task: " + taskId);
        
        taskUseCase.updateTask(taskId, title, description, startTime)
                .thenAccept(success -> {
                    if (success) {
                        setOperationSuccessful(true);
                        Log.d(TAG, "Task updated successfully: " + taskId);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = "更新任务失败：" + throwable.getMessage();
                    setError(errorMsg);
                    Log.e(TAG, "Error updating task: " + throwable.getMessage(), throwable);
                    return null;
                })
                .whenComplete((result, throwable) -> setLoading(false));
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void deleteTask(long taskId) {
        clearError();
        setLoading(true);
        
        Log.d(TAG, "Deleting task: " + taskId);
        
        taskUseCase.deleteTask(taskId)
                .thenAccept(success -> {
                    if (success) {
                        setOperationSuccessful(true);
                        Log.d(TAG, "Task deleted successfully: " + taskId);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = "删除任务失败：" + throwable.getMessage();
                    setError(errorMsg);
                    Log.e(TAG, "Error deleting task: " + throwable.getMessage(), throwable);
                    return null;
                })
                .whenComplete((result, throwable) -> setLoading(false));
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void updateTaskStatus(long taskId, boolean isCompleted) {
        clearError();
        
        Log.d(TAG, "Updating task status: " + taskId + " -> " + isCompleted);

        taskUseCase.updateTaskStatus(taskId, isCompleted)
                .thenAccept(success -> {
                    if (success) {
                        Log.d(TAG, "Task status updated successfully: " + taskId);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = "更新任务状态失败：" + throwable.getMessage();
                    setError(errorMsg);
                    Log.e(TAG, "Error updating task status: " + throwable.getMessage(), throwable);
                    return null;
                });
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void updateTaskStartTime(long taskId, long startTime) {
        clearError();
        
        Log.d(TAG, "Updating task start time: " + taskId + " -> " + startTime);
        
        taskUseCase.updateTaskStartTime(taskId, startTime)
                .thenAccept(success -> {
                    if (success) {
                        Log.d(TAG, "Task start time updated successfully: " + taskId);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = "更新任务开始时间失败：" + throwable.getMessage();
                    setError(errorMsg);
                    Log.e(TAG, "Error updating task start time: " + throwable.getMessage(), throwable);
                    return null;
                });
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void persistTaskOrder(List<TaskEntity> orderedTasks) {
        if (orderedTasks == null || orderedTasks.isEmpty()) {
            Log.e(TAG, "Cannot persist order: ordered list is null or empty");
            return;
        }
        
        clearError();
        
        Log.d(TAG, "Persisting order for " + orderedTasks.size() + " tasks");
        
        taskUseCase.persistTaskOrder(orderedTasks)
                .thenAccept(success -> {
                    if (success) {
                        Log.d(TAG, "Task order persisted successfully");
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = "持久化任务顺序失败：" + throwable.getMessage();
                    setError(errorMsg);
                    Log.e(TAG, "Error persisting task order: " + throwable.getMessage(), throwable);
                    return null;
                });
    }
    
    // 状态管理方法
    public void setSelectedTask(TaskEntity task) {
        selectedTask.setValue(task);
        Log.d(TAG, "Selected task: " + (task != null ? task.getId() : "null"));
    }
    
    public void refreshTasks() {
        Log.d(TAG, "Refreshing tasks");
        observeTasks();
    }
    
    public void clearError() {
        error.setValue(null);
    }
    
    public void clearSuccess() {
        isOperationSuccessful.setValue(false);
    }
    
    // 统计信息方法
    public int getTotalTaskCount() {
        List<TaskEntity> currentTasks = tasks.getValue();
        return currentTasks != null ? currentTasks.size() : 0;
    }
    
    public int getCompletedTaskCount() {
        List<TaskEntity> currentTasks = tasks.getValue();
        if (currentTasks == null) return 0;
        
        return (int) currentTasks.stream()
                .filter(TaskEntity::isCompleted)
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
    
    // 私有方法
    private void observeTasks() {
        // 从Repository获取任务列表
        ITaskRepository repository = AppLifecycleManager.getInstance().getDependency(ITaskRepository.class);
        repository.observeAll().observeForever(tasks -> {
            if (tasks != null) {
                this.tasks.setValue(tasks);
                Log.d(TAG, "Tasks updated: " + tasks.size() + " tasks");
            } else {
                this.tasks.setValue(null);
                Log.d(TAG, "Tasks cleared");
            }
        });
    }
    
    private void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }
    
    private void setError(String errorMessage) {
        error.setValue(errorMessage);
    }
    
    private void setOperationSuccessful(boolean successful) {
        isOperationSuccessful.setValue(successful);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        
        // 销毁作用域
        if (scope != null) {
            AppLifecycleManager.getInstance().destroyScope(scope.getName());
        }
        
        Log.d(TAG, "TaskViewModel cleared");
    }
}


