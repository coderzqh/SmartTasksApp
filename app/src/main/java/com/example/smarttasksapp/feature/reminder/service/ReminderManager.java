package com.example.smarttasksapp.feature.reminder.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.reminder.service.reciver.AlarmReceiver;

import java.util.Date;

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
        long triggerAtMillis = config.getStartTime();
        long currentTimeMillis = System.currentTimeMillis();
        
        Log.d("ReminderManager", "Setting alarm for: " + new Date(triggerAtMillis) +
              ", current time: " + new Date(currentTimeMillis) + 
              ", triggerAtMillis: " + triggerAtMillis + 
              ", currentTimeMillis: " + currentTimeMillis);
    
        // 校验开始时间是否有效且在未来
        if (triggerAtMillis <= currentTimeMillis) {
            Log.w("ReminderManager", "Alarm time is in the past, skipping alarm setup");
            return;
        }
    
        // 创建 PendingIntent
        PendingIntent pendingIntent = createPendingIntent(config);
    
        // 设置闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
        
        Log.d("ReminderManager", "Alarm set successfully");
    }

    private PendingIntent createPendingIntent(ReminderConfig config) {
        Intent intent = new Intent(context, AlarmReceiver.class)
                .putExtra("taskTitle", config.getTaskTitle())
                .putExtra("taskStartTime", config.getStartTime())
                .putExtra("taskId", config.getTaskId());

        int requestCode = generateRequestCode(config.getTaskId());

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
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
