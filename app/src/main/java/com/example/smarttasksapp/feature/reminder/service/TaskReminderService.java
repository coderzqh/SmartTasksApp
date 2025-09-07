package com.example.smarttasksapp.feature.reminder.service;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;
import com.example.smarttasksapp.feature.tasks.event.TaskEventBus;

public class TaskReminderService {
    private final ReminderManager reminderManager;
    private final TaskEventBus taskEventBus;
    
    public TaskReminderService(ReminderManager reminderManager, TaskEventBus taskEventBus) {
        this.reminderManager = reminderManager;
        this.taskEventBus = taskEventBus;
    }
    
    public void startListening() {
        taskEventBus.getEvents(TaskEventBus.TaskAddedEvent.class).observeForever(this::handleTaskAdded);
    }
    
    public void stopListening() {
        taskEventBus.getEvents(TaskEventBus.TaskAddedEvent.class).removeObserver(this::handleTaskAdded);
    }
    
    private void handleTaskAdded(TaskEventBus.TaskAddedEvent event) {
        TaskEntity task = event.getTask();
        
        // 检查任务是否有有效的开始时间
        if (task.getStartTime() > 0) {
            // 创建提醒配置
            ReminderConfig config = new ReminderConfig(
                    task.getId(),
                    task.getTitle(),
                    task.getStartTime(),
                    false // 假设默认不重复
            );
            
            // 设置闹钟
            reminderManager.setAlarm(config);
        }
    }
}