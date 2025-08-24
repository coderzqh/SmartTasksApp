package com.example.smarttasksapp.app;

import android.app.Application;
import android.util.Log;

import com.example.smarttasksapp.core.lifecycle.AppLifecycleManager;
import com.example.smarttasksapp.core.util.MemoryLeakDetector;

/**
 * 应用入口类
 * 负责初始化应用的核心组件
 */
public class SmartTasksApplication extends Application {
    private static final String TAG = "SmartTasksApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "Application onCreate");
        
        // 启动生命周期管理器
        AppLifecycleManager.getInstance().onApplicationStart(this);

        
        Log.d(TAG, "Application initialized successfully");
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        Log.d(TAG, "Application onTerminate");

        
        // 清理资源
        AppLifecycleManager.getInstance().onApplicationDestroy();
        
        Log.d(TAG, "Application terminated");
    }
}
