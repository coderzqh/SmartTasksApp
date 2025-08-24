package com.example.smarttasksapp.core.resource;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 资源管理器
 * 统一管理应用中的所有资源，确保在应用销毁时正确清理
 */
public class ResourceManager {
    private static final String TAG = "ResourceManager";
    private static ResourceManager instance;
    
    private final List<AutoCloseable> resources;
    private final List<ExecutorService> executors;
    private final List<Runnable> cleanupTasks;
    private boolean isShutdown = false;
    
    private ResourceManager() {
        this.resources = new ArrayList<>();
        this.executors = new ArrayList<>();
        this.cleanupTasks = new ArrayList<>();
    }
    
    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * 注册可关闭的资源
     */
    public void registerResource(AutoCloseable resource) {
        if (isShutdown) {
            Log.w(TAG, "ResourceManager is shutdown, cannot register new resource");
            return;
        }
        
        synchronized (resources) {
            resources.add(resource);
            Log.d(TAG, "Registered resource: " + resource.getClass().getSimpleName());
        }
    }
    
    /**
     * 注册线程池
     */
    public void registerExecutor(ExecutorService executor) {
        if (isShutdown) {
            Log.w(TAG, "ResourceManager is shutdown, cannot register new executor");
            return;
        }
        
        synchronized (executors) {
            executors.add(executor);
            Log.d(TAG, "Registered executor: " + executor.getClass().getSimpleName());
        }
    }
    
    /**
     * 注册清理任务
     */
    public void registerCleanupTask(Runnable task) {
        if (isShutdown) {
            Log.w(TAG, "ResourceManager is shutdown, cannot register new cleanup task");
            return;
        }
        
        synchronized (cleanupTasks) {
            cleanupTasks.add(task);
            Log.d(TAG, "Registered cleanup task");
        }
    }
    
    /**
     * 清理所有资源
     */
    public void cleanup() {
        if (isShutdown) {
            Log.w(TAG, "ResourceManager already shutdown");
            return;
        }
        
        Log.d(TAG, "Starting resource cleanup");
        isShutdown = true;
        
        // 执行清理任务
        synchronized (cleanupTasks) {
            for (Runnable task : cleanupTasks) {
                try {
                    task.run();
                    Log.d(TAG, "Cleanup task executed successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to execute cleanup task", e);
                }
            }
            cleanupTasks.clear();
        }
        
        // 关闭线程池
        synchronized (executors) {
            for (ExecutorService executor : executors) {
                try {
                    executor.shutdown();
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                            Log.e(TAG, "Executor did not terminate: " + executor.getClass().getSimpleName());
                        }
                    }
                    Log.d(TAG, "Executor shutdown: " + executor.getClass().getSimpleName());
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                    Log.e(TAG, "Executor shutdown interrupted", e);
                }
            }
            executors.clear();
        }
        
        // 关闭资源
        synchronized (resources) {
            for (AutoCloseable resource : resources) {
                try {
                    resource.close();
                    Log.d(TAG, "Resource closed: " + resource.getClass().getSimpleName());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to close resource: " + resource.getClass().getSimpleName(), e);
                }
            }
            resources.clear();
        }
        
        Log.d(TAG, "Resource cleanup completed");
    }
    
    /**
     * 检查是否已关闭
     */
    public boolean isShutdown() {
        return isShutdown;
    }
    
    /**
     * 获取资源数量（用于调试）
     */
    public int getResourceCount() {
        synchronized (resources) {
            return resources.size();
        }
    }
    
    /**
     * 获取线程池数量（用于调试）
     */
    public int getExecutorCount() {
        synchronized (executors) {
            return executors.size();
        }
    }
}
