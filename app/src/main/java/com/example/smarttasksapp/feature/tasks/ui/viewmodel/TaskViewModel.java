package com.example.smarttasksapp.feature.tasks.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.smarttasksapp.core.manager.BaseManager;
import com.example.smarttasksapp.core.viewmodel.BaseViewModel;
import com.example.smarttasksapp.feature.tasks.constants.TaskConstants;
import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.feature.tasks.domain.Task;
import com.example.smarttasksapp.feature.tasks.manager.TaskManager;
import com.example.smarttasksapp.feature.tasks.state.TaskState;

import java.util.List;

/**
 * 任务视图模型
 * 继承BaseViewModel，使用依赖注入和状态管理模式
 */
public class TaskViewModel extends BaseViewModel {
    private static final String MODULE_NAME = "Tasks";
    private static final String TAG = "TaskViewModel";
    private final TaskState taskState;
    private final ITaskRepository repository;
    private final TaskManager taskManager;

    public TaskViewModel(@NonNull Application application) {
        super(application, MODULE_NAME, new TaskState());
        this.taskState = (TaskState) getState();
        this.repository = appModule.getSingleton(ITaskRepository.class);
        this.taskManager = new TaskManager(application);
        
        if (this.repository == null) {
            throw new IllegalStateException("ITaskRepository not registered in AppModule");
        }
        
        // 初始化时观察任务列表
        observeTasks();
    }

    // 任务数据访问方法
    public LiveData<List<Task>> getTasks() {
        return taskState.getTasks();
    }

    public LiveData<Task> getSelectedTask() {
        return taskState.getSelectedTask();
    }

    // 任务操作方法
    public void addTask(String title, String description, long startTime) {
        if (title == null || title.trim().isEmpty()) {
            taskState.setError(TaskConstants.TEXT_TITLE_EMPTY);
            return;
        }

        taskState.setLoading(true);
        taskState.clearError();
        
        Log.d(TAG,"Adding task: " + title);
        
        taskManager.addTaskAsync(title.trim(), description != null ? description.trim() : null, startTime)
                .thenAccept(taskId -> {
                    if (taskId > 0) {
                        taskState.setOperationSuccessful(true);
                        sendSuccessEvent("addTask", taskId);
                        Log.d(TAG,"Task added successfully with ID: " + taskId);
                    } else {
                        String errorMsg = "添加任务失败：无法获取任务ID";
                        taskState.setError(errorMsg);
                        sendErrorEvent("addTask", errorMsg);
                        Log.e(TAG,"Failed to add task: " + errorMsg);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = TaskConstants.ERROR_ADD_TASK + throwable.getMessage();
                    taskState.setError(errorMsg);
                    sendErrorEvent("addTask", throwable.getMessage());
                    Log.e(TAG,"Error adding task: " + throwable.getMessage(), throwable);
                    return null;
                })
                .whenComplete((result, throwable) -> taskState.setLoading(false));
    }

    public void reorder(long fromTaskId, long toTaskId, boolean placeAbove) {
        Log.d(TAG,"Reordering tasks: " + fromTaskId + " -> " + toTaskId + " (placeAbove: " + placeAbove + ")");
        
        taskManager.reorderTasksAsync(fromTaskId, toTaskId, placeAbove)
                .thenAccept(success -> {
                    if (success) {
                        sendSuccessEvent("reorder", "Tasks reordered successfully");
                        Log.d(TAG,"Tasks reordered successfully");
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = TaskConstants.ERROR_REORDER + throwable.getMessage();
                    taskState.setError(errorMsg);
                    sendErrorEvent("reorder", throwable.getMessage());
                    Log.e(TAG,"Error reordering tasks: " + throwable.getMessage(), throwable);
                    return null;
                });
    }

    public void persistOrder(List<Task> ordered) {
        if (ordered == null || ordered.isEmpty()) {
            Log.e("","Cannot persist order: ordered list is null or empty");
            return;
        }

        Log.d(TAG,"Persisting order for " + ordered.size() + " tasks");
        
        taskManager.persistTaskOrderAsync(ordered)
                .thenAccept(success -> {
                    if (success) {
                        sendSuccessEvent("persistOrder", "Order persisted successfully");
                        Log.d(TAG,"Task order persisted successfully");
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = TaskConstants.ERROR_PERSIST_ORDER + throwable.getMessage();
                    taskState.setError(errorMsg);
                    sendErrorEvent("persistOrder", throwable.getMessage());
                    Log.e(TAG,"Error persisting task order: " + throwable.getMessage(), throwable);
                    return null;
                });
    }

    public void updateTask(long taskId, String title, String description, long startTime) {
        if (title == null || title.trim().isEmpty()) {
            taskState.setError(TaskConstants.TEXT_TITLE_EMPTY);
            return;
        }

        taskState.setLoading(true);
        taskState.clearError();

        taskManager.updateTaskAsync(taskId, title.trim(), description != null ? description.trim() : null, startTime)
                .thenAccept(success -> {
                    if (success) {
                        taskState.setOperationSuccessful(true);
                        sendSuccessEvent("updateTask", taskId);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = TaskConstants.ERROR_UPDATE_TASK + throwable.getMessage();
                    taskState.setError(errorMsg);
                    sendErrorEvent("updateTask", throwable.getMessage());
                    return null;
                })
                .whenComplete((result, throwable) -> taskState.setLoading(false));
    }
    
    public void updateTaskCompletedStatus(long taskId, boolean isCompleted) {
        
        taskManager.updateTaskStatusAsync(taskId, isCompleted)
                .thenAccept(success -> {
                    if (success) {
                        sendSuccessEvent("updateTaskStatus", "Status updated successfully");
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = TaskConstants.ERROR_UPDATE_STATUS + throwable.getMessage();
                    taskState.setError(errorMsg);
                    sendErrorEvent("updateTaskStatus", throwable.getMessage());
                    return null;
                });
    }
    
    public void updateTaskStartTime(long taskId, long startTime) {

        taskManager.executeAsync(new BaseManager.AsyncOperation<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                repository.updateTaskStartTime(taskId, startTime);
                return Boolean.TRUE;
            }
        }).thenAccept(success -> {
            if (success) {
                sendSuccessEvent("updateTaskStartTime", "Start time updated successfully");
            }
        }).exceptionally(throwable -> {
            String errorMsg = TaskConstants.ERROR_UPDATE_TIME + throwable.getMessage();
            taskState.setError(errorMsg);
            sendErrorEvent("updateTaskStartTime", throwable.getMessage());;
            return null;
        });
    }
    
    public void deleteTask(long taskId) {
        taskState.setLoading(true);
        taskState.clearError();

        
        taskManager.deleteTaskAsync(taskId)
                .thenAccept(success -> {
                    if (success) {
                        taskState.setOperationSuccessful(true);
                        sendSuccessEvent("deleteTask", taskId);
                    }
                })
                .exceptionally(throwable -> {
                    String errorMsg = TaskConstants.ERROR_DELETE_TASK + throwable.getMessage();
                    taskState.setError(errorMsg);
                    sendErrorEvent("deleteTask", throwable.getMessage());
                    return null;
                })
                .whenComplete((result, throwable) -> taskState.setLoading(false));
    }

    // 状态管理方法
    public void setSelectedTask(Task task) {
        taskState.setSelectedTask(task);
    }

    public void refreshTasks() {
        taskState.setRefreshing(true);
        
        // 重新观察任务列表
        observeTasks();
        
        // 延迟重置刷新状态，给UI一些时间显示刷新指示器
        taskState.setRefreshing(false);
    }

    // 统计信息方法
    public int getTotalTaskCount() {
        return taskState.getTotalTaskCount();
    }

    public int getCompletedTaskCount() {
        return taskState.getCompletedTaskCount();
    }

    public int getPendingTaskCount() {
        return taskState.getPendingTaskCount();
    }

    public double getCompletionRate() {
        return taskState.getCompletionRate();
    }

    // 私有方法
    private void observeTasks() {
        // 观察Repository返回的任务列表LiveData
        repository.observeAll().observeForever(tasks -> {
            if (tasks != null) {
                taskState.setTasks(tasks);
                
                // 发送数据变更事件
                sendDataChangedEvent("Task", "listUpdated", tasks);
            } else {
                taskState.setTasks(null);
            }
        });
    }

    @Override
    protected void cleanup() {
        // 清理任务管理器资源
        if (taskManager != null) {
            // TaskManager继承自BaseManager，不需要手动清理
        }
    }
}


