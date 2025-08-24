package com.example.smarttasksapp.feature.tasks.ui.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimePickerHelper {
    private final Context context;
    private final SimpleDateFormat dateFormat;
    private long selectedTime = 0;

    public DateTimePickerHelper(Context context) {
        this.context = context;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    public void setSelectedTime(long time) {
        this.selectedTime = time;
    }

    public long getSelectedTime() {
        return selectedTime;
    }

    public void showDateTimePicker(TextView targetView, OnDateTimeSelectedListener listener) {
        final Calendar calendar = Calendar.getInstance();
        
        // 如果已经选择了时间，使用已选择的时间
        if (selectedTime > 0) {
            calendar.setTimeInMillis(selectedTime);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            context,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                // 日期选择完成后显示时间选择器
                showTimePicker(calendar, targetView, listener);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void showTimePicker(final Calendar calendar, TextView targetView, OnDateTimeSelectedListener listener) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            context,
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                
                selectedTime = calendar.getTimeInMillis();
                
                if (targetView != null) {
                    targetView.setText(dateFormat.format(new Date(selectedTime)));
                    targetView.setTextColor(context.getColor(android.R.color.black));
                }
                
                if (listener != null) {
                    listener.onDateTimeSelected(selectedTime);
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        
        timePickerDialog.show();
    }

    public String formatTime(long time) {
        if (time <= 0) {
            return "未设置";
        }
        return dateFormat.format(new Date(time));
    }

    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(long selectedTime);
    }
}
