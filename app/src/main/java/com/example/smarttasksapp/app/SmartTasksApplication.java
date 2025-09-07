package com.example.smarttasksapp.app;

import android.app.Application;

import com.example.smarttasksapp.feature.reminder.service.ReminderManager;
import com.example.smarttasksapp.feature.reminder.service.TaskReminderService;
import com.example.smarttasksapp.feature.tasks.event.TaskEventBus;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class SmartTasksApplication extends Application {
    private TaskReminderService taskReminderService;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化任务提醒服务
        ReminderManager reminderManager = ReminderManager.getInstance(this);
        TaskEventBus taskEventBus = TaskEventBus.getInstance();
        taskReminderService = new TaskReminderService(reminderManager, taskEventBus);
        taskReminderService.startListening();
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // 停止任务提醒服务
        if (taskReminderService != null) {
            taskReminderService.stopListening();
        }
    }
}
