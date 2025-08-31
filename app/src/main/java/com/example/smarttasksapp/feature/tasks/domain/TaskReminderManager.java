package com.example.smarttasksapp.feature.tasks.domain;

import android.content.Context;
import android.util.Log;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.reminder.service.IReminderService;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TaskReminderManager {
    private static final String TAG = "TaskReminderManager";
    
    private final IReminderService reminderService;
    private final PriorityQueue<TaskEntity> reminderQueue;
    private final ScheduledExecutorService scheduler;
    
    @Inject
    public TaskReminderManager(IReminderService reminderService, @ApplicationContext Context context) {
        this.reminderService = reminderService;
        // 创建一个按开始时间排序的优先队列
        this.reminderQueue = new PriorityQueue<>((t1, t2) -> Long.compare(t1.getStartTime(), t2.getStartTime()));
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // 启动一个定期检查队列的任务
        scheduler.scheduleWithFixedDelay(this::processQueue, 0, 1, TimeUnit.MINUTES);
    }
    
    /**
     * 添加任务到提醒队列
     * @param task 任务实体
     */
    public void addTaskToReminderQueue(TaskEntity task) {
        if (task.getStartTime() > 0) {
            long now = System.currentTimeMillis();
            long triggerTime = task.getStartTime();
            
            // 如果任务的开始时间是最近的（在未来1分钟内），则直接设置闹钟
            if (triggerTime > now && triggerTime <= now + TimeUnit.MINUTES.toMillis(1)) {
                ReminderConfig config = new ReminderConfig(
                    task.getId(),
                    task.getTitle(),
                    triggerTime,
                    false, // 不重复
                    task.getStartTime()
                );
                reminderService.setReminder(config);
                Log.d(TAG, "Set immediate reminder for task: " + task.getTitle() + " at " + triggerTime);
            } else {
                // 否则加入优先队列
                synchronized (reminderQueue) {
                    reminderQueue.offer(task);
                    Log.d(TAG, "Task added to reminder queue: " + task.getTitle() + " at " + task.getStartTime());
                }
                processQueue();
            }
        }
    }
    
    /**
     * 从提醒队列中移除任务
     * @param taskId 任务ID
     */
    public void removeTaskFromReminderQueue(long taskId) {
        synchronized (reminderQueue) {
            reminderQueue.removeIf(task -> task.getId() == taskId);
            Log.d(TAG, "Task removed from reminder queue: " + taskId);
        }
    }
    
    /**
     * 处理提醒队列，设置最近的闹钟
     */
    private void processQueue() {
        synchronized (reminderQueue) {
            if (!reminderQueue.isEmpty()) {
                TaskEntity nextTask = reminderQueue.peek();
                if (nextTask != null) {
                    long now = System.currentTimeMillis();
                    long triggerTime = nextTask.getStartTime();
                    
                    // 如果任务的开始时间已经过去，移除它并处理下一个
                    if (triggerTime <= now) {
                        reminderQueue.poll();
                        Log.d(TAG, "Task start time has passed, removing from queue: " + nextTask.getTitle());
                        processQueue(); // 递归处理下一个任务
                        return;
                    }
                    
                    // 设置闹钟
                    ReminderConfig config = new ReminderConfig(
                        (int) nextTask.getId(),
                        nextTask.getTitle(),
                        triggerTime,
                        false, // 不重复
                        nextTask.getStartTime()
                    );
                    reminderService.setReminder(config);
                    Log.d(TAG, "Set reminder for task: " + nextTask.getTitle() + " at " + triggerTime);
                }
            }
        }
    }
    
    /**
     * 更新任务的提醒时间
     * @param task 任务实体
     */
    public void updateTaskReminder(TaskEntity task) {
        removeTaskFromReminderQueue(task.getId());
        addTaskToReminderQueue(task);
    }
}