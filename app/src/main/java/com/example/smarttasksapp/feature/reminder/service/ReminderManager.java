package com.example.smarttasksapp.feature.reminder.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.reminder.service.reciver.AlarmReceiver;

public class ReminderManager {
    private static ReminderManager instance;
    private Context context;
    private AlarmManager alarmManager;
    
    private ReminderManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    
    public static ReminderManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReminderManager(context);
        }
        return instance;
    }
    
    @SuppressLint("ScheduleExactAlarm")
    public void setAlarm(ReminderConfig config) {
        // 检查是否有有效的开始时间
        if (config.getStartTime() <= 0) {
            return;
        }
        
        // 创建Intent
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("taskTitle", config.getTaskTitle());
        intent.putExtra("taskStartTime", config.getStartTime());
        intent.putExtra("taskId", config.getTaskId());
        
        // 生成请求码
        int requestCode = generateRequestCode(config.getTaskId());
        
        // 创建PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 设置闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, config.getStartTime(), pendingIntent);
        } else {
            // Android 6.0以下
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, config.getStartTime(), pendingIntent);
        }
    }
    
    public void cancelAlarm(long taskId) {
        // 生成请求码
        int requestCode = generateRequestCode(taskId);
        
        // 创建Intent
        Intent intent = new Intent(context, AlarmReceiver.class);
        
        // 创建PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 取消闹钟
        alarmManager.cancel(pendingIntent);
    }
    
    private int generateRequestCode(long taskId) {
        // 使用任务ID和开始时间生成唯一请求码
        // 将高32位与低32位进行异或操作
        return Long.hashCode(taskId);
    }
}
