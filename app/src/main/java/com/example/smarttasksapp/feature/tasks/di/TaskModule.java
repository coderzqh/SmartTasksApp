package com.example.smarttasksapp.feature.tasks.di;

import android.content.Context;

import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.feature.tasks.ui.utils.DateTimePickerHelper;
import com.example.smarttasksapp.infrastructure.database.AppDatabase;
import com.example.smarttasksapp.infrastructure.dao.TaskDao;
import com.example.smarttasksapp.infrastructure.repository.TaskRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tasks模块依赖注入管理
 * 使用单例模式管理模块内的依赖
 */
public class TaskModule {
    private static TaskModule instance;
    private final ExecutorService executorService;
    private final TaskDao taskDao;
    private final ITaskRepository taskRepository;
    private final DateTimePickerHelper dateTimePickerHelper;

    private TaskModule(Context context) {
        // 创建线程池
        this.executorService = Executors.newFixedThreadPool(3);
        
        // 创建数据库相关依赖
        this.taskDao = AppDatabase.getInstance(context).taskDao();
        this.taskRepository = new TaskRepository(context, executorService);
        
        // 创建工具类
        this.dateTimePickerHelper = new DateTimePickerHelper(context);
    }

    public static synchronized TaskModule getInstance(Context context) {
        if (instance == null) {
            instance = new TaskModule(context.getApplicationContext());
        }
        return instance;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public TaskDao getTaskDao() {
        return taskDao;
    }

    public ITaskRepository getTaskRepository() {
        return taskRepository;
    }

    public DateTimePickerHelper getDateTimePickerHelper() {
        return dateTimePickerHelper;
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
