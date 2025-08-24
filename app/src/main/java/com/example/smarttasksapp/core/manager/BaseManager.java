package com.example.smarttasksapp.core.manager;

import android.content.Context;
import android.util.Log;

import com.example.smarttasksapp.core.di.AppModule;
import com.example.smarttasksapp.core.event.AppEventBus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 通用管理器基类
 * 提供基本的管理功能，可以被所有模块的管理器继承使用
 */
public abstract class BaseManager {
    protected final Context context;
    protected final AppModule appModule;
    protected final AppEventBus eventBus;
    protected final ExecutorService executorService;
    protected final String moduleName;
    protected final String tag;

    protected BaseManager(Context context, String moduleName) {
        this.context = context.getApplicationContext();
        this.moduleName = moduleName;
        this.tag = getClass().getSimpleName();
        this.appModule = AppModule.getInstance(context);
        this.eventBus = AppEventBus.getInstance();
        this.executorService = appModule.getGlobalExecutorService();
    }

    /**
     * 执行异步操作
     */
    public <T> CompletableFuture<T> executeAsync(AsyncOperation<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                T result = operation.execute();
                logDebug("Async operation completed successfully");
                return result;
            } catch (Exception e) {
                logError("Async operation failed: " + e.getMessage(), e);
                throw new RuntimeException("Operation failed", e);
            }
        }, executorService);
    }

    /**
     * 执行异步操作并发送事件
     */
    protected <T> CompletableFuture<T> executeAsyncWithEvent(AsyncOperation<T> operation, String operationName) {
        return executeAsync(operation)
                .thenApply(result -> {
                    sendSuccessEvent(operationName, result);
                    return result;
                })
                .exceptionally(throwable -> {
                    sendErrorEvent(operationName, throwable.getMessage(), throwable);
                    throw new RuntimeException(throwable);
                });
    }

    /**
     * 发送成功事件
     */
    protected void sendSuccessEvent(String operation, Object data) {
        AppEventBus.SuccessEvent event = new AppEventBus.SuccessEvent(
                tag, moduleName, operation, data);
        eventBus.postGlobalEvent(event);
        logDebug("Success event sent: " + operation);
    }

    /**
     * 发送错误事件
     */
    protected void sendErrorEvent(String operation, String errorMessage, Throwable exception) {
        AppEventBus.ErrorEvent event = new AppEventBus.ErrorEvent(
                tag, moduleName, errorMessage, exception instanceof Exception ? (Exception) exception : null);
        eventBus.postGlobalEvent(event);
        logError("Error event sent: " + operation + " - " + errorMessage, exception);
    }

    /**
     * 发送数据变更事件
     */
    protected void sendDataChangedEvent(String dataType, String operation, Object data) {
        AppEventBus.DataChangedEvent event = new AppEventBus.DataChangedEvent(
                tag, dataType, operation, data);
        eventBus.postGlobalEvent(event);
        logDebug("Data changed event sent: " + dataType + " - " + operation);
    }

    /**
     * 发送生命周期事件
     */
    protected void sendLifecycleEvent(String component, String lifecycle) {
        AppEventBus.LifecycleEvent event = new AppEventBus.LifecycleEvent(
                tag, component, lifecycle);
        eventBus.postGlobalEvent(event);
        logDebug("Lifecycle event sent: " + component + " - " + lifecycle);
    }

    /**
     * 日志记录方法
     */
    protected void logDebug(String message) {
        Log.d(tag, "[" + moduleName + "] " + message);
    }

    protected void logInfo(String message) {
        Log.i(tag, "[" + moduleName + "] " + message);
    }

    protected void logWarning(String message) {
        Log.w(tag, "[" + moduleName + "] " + message);
    }

    protected void logError(String message, Throwable throwable) {
        Log.e(tag, "[" + moduleName + "] " + message, throwable);
    }

    /**
     * 数据验证
     */
    protected void validateNotNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    protected void validateNotEmpty(String string, String name) {
        if (string == null || string.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
    }

    protected void validatePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    /**
     * 获取模块名称
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * 异步操作接口
     */
    @FunctionalInterface
    public interface AsyncOperation<T> {
        T execute() throws Exception;
    }
}
