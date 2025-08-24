package com.example.smarttasksapp.core.di;

import android.util.Log;

import com.example.smarttasksapp.core.resource.ResourceManager;
import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.infrastructure.repository.TaskRepositoryImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 模块注册器
 * 负责自动发现和注册所有模块
 */
public class ModuleRegistry {
    private static final String TAG = "ModuleRegistry";
    
    /**
     * 自动发现和注册所有模块
     */
    public static void autoDiscoverModules(DIContainer container) {
        Log.d(TAG, "Auto-discovering modules");
        
        // 注册任务模块
        registerTaskModule(container);
        
        // 注册其他模块
        registerOtherModules(container);
        
        Log.d(TAG, "All modules registered successfully");
    }
    
    /**
     * 注册任务模块
     */
    private static void registerTaskModule(DIContainer container) {
        Log.d(TAG, "Registering task module");
        
        // 注册接口实现
        container.registerImplementation(ITaskRepository.class, TaskRepositoryImpl.class);
        
        // 创建并注册线程池
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        container.registerSingleton(ExecutorService.class, executorService);
        
        // 将线程池注册到资源管理器
        ResourceManager.getInstance().registerExecutor(executorService);
        
        Log.d(TAG, "Task module registered with resource management");
    }
    
    /**
     * 注册其他模块
     */
    private static void registerOtherModules(DIContainer container) {
        // 这里可以注册其他模块
        // 例如：用户模块、设置模块等
        Log.d(TAG, "Other modules registered");
    }
}
