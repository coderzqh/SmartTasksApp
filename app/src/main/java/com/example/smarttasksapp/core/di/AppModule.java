package com.example.smarttasksapp.core.di;

import android.content.Context;

import com.example.smarttasksapp.infrastructure.database.AppDatabase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用级依赖注入管理
 * 统一管理整个应用的依赖，支持模块化扩展
 */
public class AppModule {
    private static AppModule instance;
    private final Context applicationContext;
    private final ConcurrentHashMap<Class<?>, Object> singletonCache;
    private final ExecutorService globalExecutorService;
    private final AppDatabase appDatabase;

    private AppModule(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.singletonCache = new ConcurrentHashMap<>();
        this.globalExecutorService = Executors.newFixedThreadPool(4);
        this.appDatabase = AppDatabase.getInstance(applicationContext);
    }

    public static synchronized AppModule getInstance(Context context) {
        if (instance == null) {
            instance = new AppModule(context);
        }
        return instance;
    }

    /**
     * 获取应用上下文
     */
    public Context getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取全局线程池
     */
    public ExecutorService getGlobalExecutorService() {
        return globalExecutorService;
    }

    /**
     * 获取数据库实例
     */
    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    /**
     * 注册单例对象
     */
    public <T> void registerSingleton(Class<T> clazz, T instance) {
        singletonCache.put(clazz, instance);
    }

    /**
     * 获取单例对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getSingleton(Class<T> clazz) {
        return (T) singletonCache.get(clazz);
    }

    /**
     * 检查是否已注册单例
     */
    public <T> boolean hasSingleton(Class<T> clazz) {
        return singletonCache.containsKey(clazz);
    }

    /**
     * 移除单例对象
     */
    public <T> void removeSingleton(Class<T> clazz) {
        singletonCache.remove(clazz);
    }

    /**
     * 清理所有单例对象
     */
    public void clearSingletons() {
        singletonCache.clear();
    }

    /**
     * 应用关闭时清理资源
     */
    public void shutdown() {
        if (globalExecutorService != null && !globalExecutorService.isShutdown()) {
            globalExecutorService.shutdown();
        }
        clearSingletons();
    }
}
