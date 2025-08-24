package com.example.smarttasksapp.core.di;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import android.content.Context;
import java.util.concurrent.ExecutorService;

/**
 * 依赖注入容器
 * 管理接口到实现的映射和依赖解析
 */
public class DIContainer {
    private static final String TAG = "DIContainer";
    
    private final Map<Class<?>, Class<?>> interfaceToImplementation;
    private final Map<Class<?>, Object> singletons;
    private final Map<Class<?>, Provider<?>> providers;
    
    public DIContainer() {
        this.interfaceToImplementation = new ConcurrentHashMap<>();
        this.singletons = new ConcurrentHashMap<>();
        this.providers = new ConcurrentHashMap<>();
    }
    
    /**
     * 注册接口实现
     */
    public <T> void registerImplementation(Class<T> interfaceType, Class<? extends T> implementationType) {
        interfaceToImplementation.put(interfaceType, implementationType);
        Log.d(TAG, "Registered implementation: " + interfaceType.getSimpleName() + " -> " + implementationType.getSimpleName());
    }
    
    /**
     * 注册单例
     */
    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
        Log.d(TAG, "Registered singleton: " + type.getSimpleName());
    }
    
    /**
     * 注册提供者
     */
    public <T> void registerProvider(Class<T> type, Provider<T> provider) {
        providers.put(type, provider);
        Log.d(TAG, "Registered provider: " + type.getSimpleName());
    }
    
    /**
     * 注册模块
     */
    public void registerModule(Class<?> moduleClass) {
        try {
            // 这里可以实现模块自动注册逻辑
            Log.d(TAG, "Registered module: " + moduleClass.getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "Failed to register module: " + moduleClass.getSimpleName(), e);
        }
    }
    
    /**
     * 获取依赖
     */
    @SuppressWarnings("unchecked")
    public <T> T getDependency(Class<T> type) {
        // 先检查单例
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }
        
        // 检查提供者
        Provider<?> provider = providers.get(type);
        if (provider != null) {
            return (T) provider.get();
        }
        
        // 检查接口实现映射
        Class<?> implementationType = interfaceToImplementation.get(type);
        if (implementationType != null) {
            try {
                Object instance = createInstance(implementationType);
                @SuppressWarnings("unchecked")
                T typedInstance = (T) instance;
                return typedInstance;
            } catch (Exception e) {
                Log.e(TAG, "Failed to create instance of " + implementationType.getName(), e);
                throw new RuntimeException("Failed to create instance", e);
            }
        }
        
        // 尝试直接实例化
        try {
            Object instance = createInstance(type);
            @SuppressWarnings("unchecked")
            T typedInstance = (T) instance;
            return typedInstance;
        } catch (Exception e) {
            Log.e(TAG, "Failed to create instance of " + type.getName(), e);
            throw new RuntimeException("Failed to create instance of " + type.getName(), e);
        }
    }
    
    /**
     * 创建实例
     */
    private Object createInstance(Class<?> type) throws Exception {
        // 特殊处理TaskRepositoryImpl，它需要Context和ExecutorService参数
        if (type == com.example.smarttasksapp.infrastructure.repository.TaskRepositoryImpl.class) {
            // 获取Context和ExecutorService依赖
            Context context = getContext();
            ExecutorService executorService = getExecutorService();
            
            if (context != null && executorService != null) {
                Constructor<?> constructor = type.getDeclaredConstructor(Context.class, ExecutorService.class);
                constructor.setAccessible(true);
                Object instance = constructor.newInstance(context, executorService);
                Log.d(TAG, "Created TaskRepositoryImpl instance with parameters");
                return instance;
            }
        }
        
        // 尝试无参构造函数
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            Log.d(TAG, "Created instance: " + type.getSimpleName());
            return instance;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "No default constructor found for " + type.getName(), e);
            throw new RuntimeException("Cannot create instance of " + type.getName() + " - no default constructor", e);
        }
    }
    
    /**
     * 获取Context依赖
     */
    private Context getContext() {
        return (Context) singletons.get(Context.class);
    }
    
    /**
     * 获取ExecutorService依赖
     */
    private ExecutorService getExecutorService() {
        return (ExecutorService) singletons.get(java.util.concurrent.ExecutorService.class);
    }
    
    /**
     * 检查是否已注册
     */
    public boolean isRegistered(Class<?> type) {
        return singletons.containsKey(type) || 
               providers.containsKey(type) || 
               interfaceToImplementation.containsKey(type);
    }
    
    /**
     * 清理容器
     */
    public void clear() {
        interfaceToImplementation.clear();
        singletons.clear();
        providers.clear();
        Log.d(TAG, "DIContainer cleared");
    }
}
