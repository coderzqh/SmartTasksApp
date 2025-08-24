package com.example.smarttasksapp.app;

import android.app.Application;

import com.example.smarttasksapp.core.AppInitializer;

/**
 * 应用入口类
 * 负责初始化应用的核心组件
 */
public class SmartTasksApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化应用核心组件
        AppInitializer.initialize(this);
    }
}
