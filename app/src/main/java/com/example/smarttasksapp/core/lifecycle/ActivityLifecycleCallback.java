package com.example.smarttasksapp.core.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.example.smarttasksapp.core.util.MemoryLeakDetector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Activity生命周期回调
 * 管理Activity级别的生命周期和资源清理
 */
public class ActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ActivityLifecycleCallback";
    
    private final Map<Activity, String> activityScopes;
    private final AppLifecycleManager lifecycleManager;
    
    public ActivityLifecycleCallback(AppLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
        this.activityScopes = new ConcurrentHashMap<>();
    }
    
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String scopeName = "Activity_" + activity.getClass().getSimpleName() + "_" + activity.hashCode();
        activityScopes.put(activity, scopeName);
        
        // 注册Activity到内存泄漏检测器
        MemoryLeakDetector.getInstance().registerActivity(activity);
        
        Log.d(TAG, "Activity created: " + activity.getClass().getSimpleName() + " with scope: " + scopeName);
    }
    
    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, "Activity started: " + activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "Activity resumed: " + activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "Activity paused: " + activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "Activity stopped: " + activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(TAG, "Activity save instance state: " + activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityDestroyed(Activity activity) {
        String scopeName = activityScopes.remove(activity);
        if (scopeName != null) {
            // 销毁Activity相关的Scope
            lifecycleManager.destroyScope(scopeName);
            Log.d(TAG, "Activity destroyed: " + activity.getClass().getSimpleName() + " with scope: " + scopeName);
        } else {
            Log.w(TAG, "Activity destroyed but no scope found: " + activity.getClass().getSimpleName());
        }
    }
    
    /**
     * 获取Activity的作用域名称
     */
    public String getActivityScopeName(Activity activity) {
        return activityScopes.get(activity);
    }
    
    /**
     * 检查Activity是否有作用域
     */
    public boolean hasActivityScope(Activity activity) {
        return activityScopes.containsKey(activity);
    }
    
    /**
     * 获取当前活动的Activity数量
     */
    public int getActiveActivityCount() {
        return activityScopes.size();
    }
}
