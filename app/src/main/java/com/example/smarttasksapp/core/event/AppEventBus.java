package com.example.smarttasksapp.core.event;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用级事件总线
 * 统一处理整个应用的事件通信，支持模块化事件管理
 */
public class AppEventBus {
    private static AppEventBus instance;
    private final ConcurrentHashMap<String, MutableLiveData<AppEvent>> eventChannels;
    private final MutableLiveData<AppEvent> globalEventBus;

    private AppEventBus() {
        this.eventChannels = new ConcurrentHashMap<>();
        this.globalEventBus = new MutableLiveData<>();
    }

    public static synchronized AppEventBus getInstance() {
        if (instance == null) {
            instance = new AppEventBus();
        }
        return instance;
    }

    /**
     * 获取全局事件流
     */
    public LiveData<AppEvent> getGlobalEvents() {
        return globalEventBus;
    }

    /**
     * 获取指定频道的事件流
     */
    public LiveData<AppEvent> getChannelEvents(String channelName) {
        return eventChannels.computeIfAbsent(channelName, k -> new MutableLiveData<>());
    }

    /**
     * 发送全局事件
     */
    public void postGlobalEvent(AppEvent event) {
        globalEventBus.postValue(event);
    }

    /**
     * 发送频道事件
     */
    public void postChannelEvent(String channelName, AppEvent event) {
        MutableLiveData<AppEvent> channel = eventChannels.get(channelName);
        if (channel != null) {
            channel.postValue(event);
        }
    }

    /**
     * 移除频道
     */
    public void removeChannel(String channelName) {
        eventChannels.remove(channelName);
    }

    /**
     * 清理所有频道
     */
    public void clearAllChannels() {
        eventChannels.clear();
    }

    /**
     * 应用事件基类
     */
    public static abstract class AppEvent {
        private final long timestamp;
        private final String source;

        protected AppEvent(String source) {
            this.timestamp = System.currentTimeMillis();
            this.source = source;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getSource() {
            return source;
        }
    }

    /**
     * 错误事件
     */
    public static class ErrorEvent extends AppEvent {
        private final String errorMessage;
        private final Exception exception;
        private final String module;

        public ErrorEvent(String source, String module, String errorMessage) {
            super(source);
            this.module = module;
            this.errorMessage = errorMessage;
            this.exception = null;
        }

        public ErrorEvent(String source, String module, String errorMessage, Exception exception) {
            super(source);
            this.module = module;
            this.errorMessage = errorMessage;
            this.exception = exception;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Exception getException() {
            return exception;
        }

        public String getModule() {
            return module;
        }
    }

    /**
     * 操作成功事件
     */
    public static class SuccessEvent extends AppEvent {
        private final String operation;
        private final String module;
        private final Object data;

        public SuccessEvent(String source, String module, String operation) {
            super(source);
            this.module = module;
            this.operation = operation;
            this.data = null;
        }

        public SuccessEvent(String source, String module, String operation, Object data) {
            super(source);
            this.module = module;
            this.operation = operation;
            this.data = data;
        }

        public String getOperation() {
            return operation;
        }

        public String getModule() {
            return module;
        }

        public Object getData() {
            return data;
        }
    }

    /**
     * 数据变更事件
     */
    public static class DataChangedEvent extends AppEvent {
        private final String dataType;
        private final String operation;
        private final Object data;

        public DataChangedEvent(String source, String dataType, String operation, Object data) {
            super(source);
            this.dataType = dataType;
            this.operation = operation;
            this.data = data;
        }

        public String getDataType() {
            return dataType;
        }

        public String getOperation() {
            return operation;
        }

        public Object getData() {
            return data;
        }
    }

    /**
     * 生命周期事件
     */
    public static class LifecycleEvent extends AppEvent {
        private final String lifecycle;
        private final String component;

        public LifecycleEvent(String source, String component, String lifecycle) {
            super(source);
            this.component = component;
            this.lifecycle = lifecycle;
        }

        public String getLifecycle() {
            return lifecycle;
        }

        public String getComponent() {
            return component;
        }
    }
}
