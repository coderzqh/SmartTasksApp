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
        taskEventBus.getEvents(TaskEventBus.TaskUpdatedEvent.class).observeForever(this::handleTaskUpdated);
        taskEventBus.getEvents(TaskEventBus.TaskDeletedEvent.class).observeForever(this::handleTaskDeleted);
    }
    
    public void stopListening() {
        taskEventBus.getEvents(TaskEventBus.TaskAddedEvent.class).removeObserver(this::handleTaskAdded);
        taskEventBus.getEvents(TaskEventBus.TaskUpdatedEvent.class).removeObserver(this::handleTaskUpdated);
        taskEventBus.getEvents(TaskEventBus.TaskDeletedEvent.class).removeObserver(this::handleTaskDeleted);
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
    
    private void handleTaskUpdated(TaskEventBus.TaskUpdatedEvent event) {
        TaskEntity task = event.getTask();
        
        // 取消之前的闹钟
        reminderManager.cancelAlarm(task.getId());
        
        // 检查任务是否有有效的开始时间
        if (task.getStartTime() > 0) {
            // 创建新的提醒配置
            ReminderConfig config = new ReminderConfig(
                    task.getId(),
                    task.getTitle(),
                    task.getStartTime(),
                    false // 假设默认不重复
            );
            
            // 设置新的闹钟
            reminderManager.setAlarm(config);
        }
    }
    
    private void handleTaskDeleted(TaskEventBus.TaskDeletedEvent event) {
        long taskId = event.getTaskId();
        
        // 取消闹钟
        reminderManager.cancelAlarm(taskId);
    }
}