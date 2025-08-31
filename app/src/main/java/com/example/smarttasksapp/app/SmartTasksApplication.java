package com.example.smarttasksapp.app;

import android.app.Application;
import android.util.Log;

import dagger.hilt.android.HiltAndroidApp;

/**
 * 应用入口类
 * 负责初始化应用的核心组件
 */
@HiltAndroidApp
public class SmartTasksApplication extends Application {
    private static final String TAG = "SmartTasksApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();

    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}
