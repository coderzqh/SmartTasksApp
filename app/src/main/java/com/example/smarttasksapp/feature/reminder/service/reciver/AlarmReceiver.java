package com.example.smarttasksapp.feature.reminder.service.reciver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.smarttasksapp.feature.reminder.domain.ReminderConfig;
import com.example.smarttasksapp.feature.reminder.service.ReminderManager;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("taskTitle");
        long taskStartTime = intent.getLongExtra("taskStartTime", 0);
        long taskId = intent.getLongExtra("taskId", 0);

        // 显示通知
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "task_reminder_channel",
                    "任务提醒",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_reminder_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("任务提醒")
                .setContentText("任务开始时间到了：" + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());

        // 设置下一个闹钟（如果需要）
        // 这里简化处理，实际应用中可能需要更复杂的逻辑来确定下一个提醒时间
        // 例如，可以设置每天重复提醒，或者根据任务的重复规则设置下一个提醒
        if (taskStartTime > 0) {
            long nextTriggerTime = taskStartTime + 24 * 60 * 60 * 1000; // 24小时后
            ReminderConfig nextConfig = new ReminderConfig(
                taskId,
                taskTitle,
                nextTriggerTime,
                false, // 不重复
                nextTriggerTime
            );
            ReminderManager.getInstance(context).setAlarm(nextConfig);
        }
    }
}
