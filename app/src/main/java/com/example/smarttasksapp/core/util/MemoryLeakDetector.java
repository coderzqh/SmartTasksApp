package com.example.smarttasksapp.core.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 内存泄漏检测器
 * 用于检测潜在的内存泄漏问题
 */
public class MemoryLeakDetector {
    private static final String TAG = "MemoryLeakDetector";
    private static MemoryLeakDetector instance;
    
    private final List<WeakReference<Activity>> activityReferences;
    private final List<WeakReference<Object>> objectReferences;
    private final ScheduledExecutorService scheduler;
    private boolean isEnabled = false;
    
    private MemoryLeakDetector() {
        this.activityReferences = new ArrayList<>();
        this.objectReferences = new ArrayList<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public static synchronized MemoryLeakDetector getInstance() {
        if (instance == null) {
            instance = new MemoryLeakDetector();
        }
        return instance;
    }
    
    /**
     * 启用内存泄漏检测
     */
    public void enable(Context context) {
        if (isEnabled) {
            Log.w(TAG, "Memory leak detection already enabled");
            return;
        }
        
        isEnabled = true;
        Log.d(TAG, "Memory leak detection enabled");
        
        // 启动定期检测
        scheduler.scheduleAtFixedRate(this::checkForLeaks, 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 禁用内存泄漏检测
     */
    public void disable() {
        if (!isEnabled) {
            return;
        }
        
        isEnabled = false;
        scheduler.shutdown();
        Log.d(TAG, "Memory leak detection disabled");
    }
    
    /**
     * 注册Activity引用
     */
    public void registerActivity(Activity activity) {
        if (!isEnabled) return;
        
        synchronized (activityReferences) {
            activityReferences.add(new WeakReference<>(activity));
            Log.d(TAG, "Registered activity: " + activity.getClass().getSimpleName());
        }
    }
    
    /**
     * 注册对象引用
     */
    public void registerObject(Object object, String tag) {
        if (!isEnabled) return;
        
        synchronized (objectReferences) {
            objectReferences.add(new WeakReference<>(object));
            Log.d(TAG, "Registered object: " + tag + " (" + object.getClass().getSimpleName() + ")");
        }
    }
    
    /**
     * 检查内存泄漏
     */
    private void checkForLeaks() {
        if (!isEnabled) return;
        
        Log.d(TAG, "Checking for memory leaks...");
        
        // 检查Activity泄漏
        checkActivityLeaks();
        
        // 检查对象泄漏
        checkObjectLeaks();
        
        // 清理无效引用
        cleanupReferences();
    }
    
    /**
     * 检查Activity泄漏
     */
    private void checkActivityLeaks() {
        synchronized (activityReferences) {
            List<WeakReference<Activity>> validRefs = new ArrayList<>();
            
            for (WeakReference<Activity> ref : activityReferences) {
                Activity activity = ref.get();
                if (activity == null) {
                    Log.w(TAG, "Potential Activity memory leak detected - Activity was garbage collected");
                } else if (activity.isFinishing() || activity.isDestroyed()) {
                    Log.w(TAG, "Activity should be destroyed: " + activity.getClass().getSimpleName());
                } else {
                    validRefs.add(ref);
                }
            }
            
            activityReferences.clear();
            activityReferences.addAll(validRefs);
        }
    }
    
    /**
     * 检查对象泄漏
     */
    private void checkObjectLeaks() {
        synchronized (objectReferences) {
            List<WeakReference<Object>> validRefs = new ArrayList<>();
            
            for (WeakReference<Object> ref : objectReferences) {
                Object obj = ref.get();
                if (obj == null) {
                    Log.w(TAG, "Potential object memory leak detected - Object was garbage collected");
                } else {
                    validRefs.add(ref);
                }
            }
            
            objectReferences.clear();
            objectReferences.addAll(validRefs);
        }
    }
    
    /**
     * 清理无效引用
     */
    private void cleanupReferences() {
        synchronized (activityReferences) {
            activityReferences.removeIf(ref -> ref.get() == null);
        }
        
        synchronized (objectReferences) {
            objectReferences.removeIf(ref -> ref.get() == null);
        }
    }
    
    /**
     * 获取检测统计信息
     */
    public String getStatistics() {
        synchronized (activityReferences) {
            synchronized (objectReferences) {
                return String.format("MemoryLeakDetector Stats - Activities: %d, Objects: %d", 
                    activityReferences.size(), objectReferences.size());
            }
        }
    }
    
    /**
     * 强制垃圾回收并检查
     */
    public void forceGcAndCheck() {
        Log.d(TAG, "Forcing garbage collection and checking for leaks");
        
        System.gc();
        
        try {
            Thread.sleep(100); // 给GC一些时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        checkForLeaks();
    }
}
