package com.example.smarttasksapp.core.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smarttasksapp.core.di.AppModule;
import com.example.smarttasksapp.core.event.AppEventBus;
import com.example.smarttasksapp.core.state.BaseState;

/**
 * 通用ViewModel基类
 * 提供基本的ViewModel功能，可以被所有模块的ViewModel继承使用
 */
public abstract class BaseViewModel extends AndroidViewModel {
    protected final AppModule appModule;
    protected final AppEventBus eventBus;
    protected final String moduleName;
    protected final BaseState state;

    protected BaseViewModel(@NonNull Application application, String moduleName, BaseState state) {
        super(application);
        this.moduleName = moduleName;
        this.state = state;
        this.appModule = AppModule.getInstance(application);
        this.eventBus = AppEventBus.getInstance();
        
        // 初始化时观察全局事件
        observeGlobalEvents();
    }

    /**
     * 获取状态对象
     */
    protected BaseState getState() {
        return state;
    }

    /**
     * 状态访问方法
     */
    public LiveData<Boolean> getIsLoading() {
        return state.getIsLoading();
    }

    public LiveData<String> getErrorMessage() {
        return state.getErrorMessage();
    }

    public LiveData<Boolean> getIsOperationSuccessful() {
        return state.getIsOperationSuccessful();
    }

    public LiveData<Boolean> getIsRefreshing() {
        return state.getIsRefreshing();
    }

    /**
     * 状态操作方法
     */
    public void clearError() {
        state.clearError();
    }

    public void clearSuccess() {
        state.clearSuccess();
    }

    public void reset() {
        state.reset();
    }

    /**
     * 事件操作方法
     */
    protected void sendSuccessEvent(String operation, Object data) {
        AppEventBus.SuccessEvent event = new AppEventBus.SuccessEvent(
                getClass().getSimpleName(), moduleName, operation, data);
        eventBus.postGlobalEvent(event);
    }

    protected void sendErrorEvent(String operation, String errorMessage) {
        AppEventBus.ErrorEvent event = new AppEventBus.ErrorEvent(
                getClass().getSimpleName(), moduleName, errorMessage);
        eventBus.postGlobalEvent(event);
    }

    protected void sendDataChangedEvent(String dataType, String operation, Object data) {
        AppEventBus.DataChangedEvent event = new AppEventBus.DataChangedEvent(
                getClass().getSimpleName(), dataType, operation, data);
        eventBus.postGlobalEvent(event);
    }

    /**
     * 观察全局事件
     */
    protected void observeGlobalEvents() {
        // 子类可以重写此方法来观察特定事件
    }

    /**
     * 获取模块名称
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * 获取应用模块
     */
    protected AppModule getAppModule() {
        return appModule;
    }

    /**
     * 获取事件总线
     */
    protected AppEventBus getEventBus() {
        return eventBus;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理资源
        cleanup();
    }

    /**
     * 清理资源，子类可以重写
     */
    protected void cleanup() {
        // 默认空实现
    }
}
