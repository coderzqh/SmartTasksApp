package com.example.smarttasksapp.feature.reminder.domain;

public class ReminderConfig {
    private final long taskId;           // 唯一任务ID
    private final String taskTitle;      // 任务名称
    private final boolean repeat;       // 是否重复提醒
    private final long startTime;   // 任务开始时间

    public ReminderConfig(long taskId, String taskTitle,long taskStartTime, boolean repeat) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.repeat = repeat;
        this.startTime = taskStartTime;
    }


    public long getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }


    public boolean isRepeat() {
        return repeat;
    }

    public long getStartTime() {
        return startTime;
    }
}
