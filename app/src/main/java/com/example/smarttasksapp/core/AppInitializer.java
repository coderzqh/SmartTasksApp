package com.example.smarttasksapp.core;

import android.content.Context;

import com.example.smarttasksapp.core.di.AppModule;
import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.infrastructure.repository.TaskRepository;

/**
 * 应用初始化器
 * 负责初始化应用的核心组件和注册依赖
 */
public class AppInitializer {
    private static boolean isInitialized = false;

    /**
     * 初始化应用
     */
    public static synchronized void initialize(Context context) {
        if (isInitialized) {
            return;
        }

        try {
            // 初始化应用模块
            AppModule appModule = AppModule.getInstance(context);
            
            // 注册任务模块的依赖
            registerTaskDependencies(appModule);
            
            // 注册其他模块的依赖
            registerOtherDependencies(appModule);
            
            isInitialized = true;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize application", e);
        }
    }

    /**
     * 注册任务模块的依赖
     */
    private static void registerTaskDependencies(AppModule appModule) {
        // 注册TaskRepository
        ITaskRepository taskRepository = new TaskRepository(
                appModule.getApplicationContext(), 
                appModule.getGlobalExecutorService()
        );
        appModule.registerSingleton(ITaskRepository.class, taskRepository);
        System.out.println("TaskRepository registered successfully in AppModule");
    }

    /**
     * 注册其他模块的依赖
     */
    private static void registerOtherDependencies(AppModule appModule) {
        // 这里可以注册其他模块的依赖
        // 例如：用户模块、设置模块等
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * 重置初始化状态（主要用于测试）
     */
    public static void reset() {
        isInitialized = false;
    }
}
