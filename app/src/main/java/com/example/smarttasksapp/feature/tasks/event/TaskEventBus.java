package com.example.smarttasksapp.feature.tasks.event;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;

import java.util.List;

/**
 * 任务事件总线
 * 处理任务相关的事件通信
 */
public class TaskEventBus {
    private static TaskEventBus instance;
    private final MutableLiveData<TaskEvent> eventBus = new MutableLiveData<>();

    private TaskEventBus() {}

    public static synchronized TaskEventBus getInstance() {
        if (instance == null) {
            instance = new TaskEventBus();
        }
        return instance;
    }

    public LiveData<TaskEvent> getEvents() {
        return eventBus;
    }
    
    public <T extends TaskEvent> LiveData<T> getEvents(Class<T> eventType) {
        // 创建一个过滤特定事件类型的LiveData
        MutableLiveData<T> filteredLiveData = new MutableLiveData<>();
        
        // 观察所有事件，并只转发指定类型的事件
        eventBus.observeForever(event -> {
            if (eventType.isInstance(event)) {
                filteredLiveData.postValue(eventType.cast(event));
            }
        });
        
        return filteredLiveData;
    }

    public void postEvent(TaskEvent event) {
        eventBus.postValue(event);
    }

    /**
     * 任务事件基类
     */
    public static abstract class TaskEvent {
        private final long timestamp;

        protected TaskEvent() {
            this.timestamp = System.currentTimeMillis();
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * 任务添加事件
     */
    public static class TaskAddedEvent extends TaskEvent {
        private final TaskEntity task;

        public TaskAddedEvent(TaskEntity task) {
            this.task = task;
        }

        public TaskEntity getTask() {
            return task;
        }
    }

    /**
     * 任务更新事件
     */
    public static class TaskUpdatedEvent extends TaskEvent {
        private final TaskEntity task;

        public TaskUpdatedEvent(TaskEntity task) {
            this.task = task;
        }

        public TaskEntity getTask() {
            return task;
        }
    }

    /**
     * 任务删除事件
     */
    public static class TaskDeletedEvent extends TaskEvent {
        private final long taskId;

        public TaskDeletedEvent(long taskId) {
            this.taskId = taskId;
        }

        public long getTaskId() {
            return taskId;
        }
    }

    /**
     * 任务状态变更事件
     */
    public static class TaskStatusChangedEvent extends TaskEvent {
        private final long taskId;
        private final boolean isCompleted;

        public TaskStatusChangedEvent(long taskId, boolean isCompleted) {
            this.taskId = taskId;
            this.isCompleted = isCompleted;
        }

        public long getTaskId() {
            return taskId;
        }

        public boolean isCompleted() {
            return isCompleted;
        }
    }

    /**
     * 任务列表更新事件
     */
    public static class TaskListUpdatedEvent extends TaskEvent {
        private final List<TaskEntity> tasks;

        public TaskListUpdatedEvent(List<TaskEntity> tasks) {
            this.tasks = tasks;
        }

        public List<TaskEntity> getTasks() {
            return tasks;
        }
    }

    /**
     * 错误事件
     */
    public static class TaskErrorEvent extends TaskEvent {
        private final String errorMessage;
        private final Exception exception;

        public TaskErrorEvent(String errorMessage) {
            this.errorMessage = errorMessage;
            this.exception = null;
        }

        public TaskErrorEvent(String errorMessage, Exception exception) {
            this.errorMessage = errorMessage;
            this.exception = exception;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Exception getException() {
            return exception;
        }
    }

    /**
     * 操作成功事件
     */
    public static class TaskOperationSuccessEvent extends TaskEvent {
        private final String operation;
        private final Object data;

        public TaskOperationSuccessEvent(String operation) {
            this.operation = operation;
            this.data = null;
        }

        public TaskOperationSuccessEvent(String operation, Object data) {
            this.operation = operation;
            this.data = data;
        }

        public String getOperation() {
            return operation;
        }

        public Object getData() {
            return data;
        }
    }
}
