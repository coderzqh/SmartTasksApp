package com.example.smarttasksapp.core.state;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 通用状态管理基类
 * 提供基本的状态管理功能，可以被所有模块继承使用
 */
public abstract class BaseState {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOperationSuccessful = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);

    // 加载状态
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    // 错误状态
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setError(String error) {
        errorMessage.setValue(error);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    // 操作成功状态
    public LiveData<Boolean> getIsOperationSuccessful() {
        return isOperationSuccessful;
    }

    public void setOperationSuccessful(boolean successful) {
        isOperationSuccessful.setValue(successful);
    }

    public void clearSuccess() {
        isOperationSuccessful.setValue(false);
    }

    // 刷新状态
    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        isRefreshing.setValue(refreshing);
    }

    // 重置所有状态
    public void reset() {
        setLoading(false);
        setRefreshing(false);
        clearError();
        clearSuccess();
        onReset();
    }

    /**
     * 子类可以重写此方法添加自定义的重置逻辑
     */
    protected void onReset() {
        // 默认空实现
    }

    /**
     * 检查是否有错误
     */
    public boolean hasError() {
        return errorMessage.getValue() != null;
    }

    /**
     * 检查是否正在加载
     */
    public boolean isCurrentlyLoading() {
        return Boolean.TRUE.equals(isLoading.getValue());
    }

    /**
     * 检查是否正在刷新
     */
    public boolean isCurrentlyRefreshing() {
        return Boolean.TRUE.equals(isRefreshing.getValue());
    }

    /**
     * 检查操作是否成功
     */
    public boolean isOperationCurrentlySuccessful() {
        return Boolean.TRUE.equals(isOperationSuccessful.getValue());
    }
}
