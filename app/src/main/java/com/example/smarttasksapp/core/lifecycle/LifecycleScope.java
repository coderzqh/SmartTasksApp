package com.example.smarttasksapp.core.lifecycle;

import android.util.Log;

import com.example.smarttasksapp.core.di.DIContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生命周期作用域
 * 管理特定作用域内的对象生命周期
 */
public class LifecycleScope {
    private static final String TAG = "LifecycleScope";
    
    private final String name;
    private final DIContainer diContainer;
    private final Map<Class<?>, Object> scopedObjects;
    private boolean isDestroyed = false;
    
    public LifecycleScope(String name, DIContainer diContainer) {
        this.name = name;
        this.diContainer = diContainer;
        this.scopedObjects = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取作用域名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取依赖
     */
    public <T> T getDependency(Class<T> type) {
        if (isDestroyed) {
            throw new IllegalStateException("Scope " + name + " is destroyed");
        }
        
        // 先检查作用域内是否已有实例
        if (scopedObjects.containsKey(type)) {
            @SuppressWarnings("unchecked")
            T instance = (T) scopedObjects.get(type);
            return instance;
        }
        
        // 从容器获取新实例
        T instance = diContainer.getDependency(type);
        scopedObjects.put(type, instance);
        
        Log.d(TAG, "Created scoped object: " + type.getSimpleName() + " in scope: " + name);
        return instance;
    }
    
    /**
     * 注册作用域对象
     */
    public <T> void registerObject(Class<T> type, T instance) {
        if (isDestroyed) {
            throw new IllegalStateException("Scope " + name + " is destroyed");
        }
        
        scopedObjects.put(type, instance);
        Log.d(TAG, "Registered object: " + type.getSimpleName() + " in scope: " + name);
    }
    
    /**
     * 销毁作用域
     */
    public void destroy() {
        if (isDestroyed) {
            return;
        }
        
        Log.d(TAG, "Destroying scope: " + name);
        
        // 清理作用域内的对象
        scopedObjects.clear();
        
        isDestroyed = true;
        Log.d(TAG, "Scope destroyed: " + name);
    }
    
    /**
     * 检查是否已销毁
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }
}
