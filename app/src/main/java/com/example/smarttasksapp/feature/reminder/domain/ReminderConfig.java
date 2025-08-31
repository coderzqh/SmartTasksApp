package com.example.smarttasksapp.feature.reminder.domain;

public class ReminderConfig {
    private final long taskId;           // 唯一任务ID
    private final String taskTitle;      // 任务名称
    private final long triggerTime;     // 提醒时间（时间戳）
    private final boolean repeat;       // 是否重复提醒
    private final long taskStartTime;   // 任务开始时间

    public ReminderConfig(long taskId, String taskTitle, long triggerTime, boolean repeat, long taskStartTime) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.triggerTime = triggerTime;
        this.repeat = repeat;
        this.taskStartTime = taskStartTime;
    }

    public ReminderConfig(long taskId, String taskTitle, long triggerTime, boolean repeat) {
        this(taskId, taskTitle, triggerTime, repeat, 0);
    }

    public long getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }
}
