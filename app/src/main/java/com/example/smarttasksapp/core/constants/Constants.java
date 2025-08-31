package com.example.smarttasksapp.core.constants;

public class Constants {
    // 数据库相关
    public static final String TABLE_NAME = "tasks";
    
    // 缓存相关
    public static final String PREF_NAME = "add_task_cache";
    public static final String KEY_TITLE = "cached_title";
    public static final String KEY_DESCRIPTION = "cached_description";
    public static final String KEY_START_TIME = "cached_start_time";
    
    // 时间格式
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    
    // 颜色常量
    public static final String COLOR_COMPLETED = "#FF9800"; // 橙色
    public static final String COLOR_PENDING = "#4CAF50";   // 绿色
    public static final String COLOR_GRAY = "#9E9E9E";      // 灰色

    
    // 任务状态常量
    public static final String COMPLETE = "完成";
    public static final String INCOMPLETE = "未完成";
    
    // 通用常量
    public static final String NOT_SET = "未设置";
    public static final String SELECT_TIME = "选择时间";
    
    // 标题相关常量
    public static final String TITLE_MAX_TWO_LINES = "标题最多两行";
    public static final String TITLE_CANNOT_BE_EMPTY = "标题不能为空";
    
    // 图标常量
    public static final String ICON_UNDO = "↺";
    public static final String ICON_CHECK = "✓";
    
    // 错误消息
    public static final String ADD_TASK_FAILED = "添加任务失败: ";
    public static final String ERROR_ADDING_TASK = "添加任务时出错: ";
    public static final String UPDATE_TASK_FAILED = "更新任务失败: ";
    public static final String ERROR_UPDATING_TASK = "更新任务时出错: ";
    public static final String DELETE_TASK_FAILED = "删除任务失败: ";
    public static final String ERROR_DELETING_TASK = "删除任务时出错: ";
    public static final String UPDATE_TASK_STATUS_FAILED = "更新任务状态失败: ";
    public static final String ERROR_UPDATING_TASK_STATUS = "更新任务状态时出错: ";
    public static final String UPDATE_TASK_START_TIME_FAILED = "更新任务开始时间失败: ";
    public static final String ERROR_UPDATING_TASK_START_TIME = "更新任务开始时间时出错: ";
    public static final String PERSIST_TASK_ORDER_FAILED = "保存任务排序失败: ";
    public static final String ERROR_PERSISTING_TASK_ORDER = "保存任务排序时出错: ";
    public static final String ERROR_REORDER = "重新排序失败: ";
    public static final String ERROR_PERSIST_ORDER = "保存排序失败: ";
    public static final String ERROR_UPDATE_STATUS = "更新任务状态失败: ";
    public static final String ERROR_UPDATE_TIME = "更新开始时间失败: ";
    
    // 确认对话框
    public static final String DIALOG_DELETE_TITLE = "确认删除";
    public static final String DIALOG_DELETE_MESSAGE = "确定要删除这个任务吗？删除后无法恢复。";
    public static final String DIALOG_DELETE_CONFIRM = "确认删除";
    public static final String DIALOG_DELETE_CANCEL = "取消";
    public static final String DIALOG_DELETE_SUCCESS = "任务已删除";
    
    // 任务详情相关常量
    public static final String TASK_SAVED = "任务已保存";
    public static final String CONFIRM_DELETE_TITLE = "确认删除";
    public static final String CONFIRM_DELETE_MESSAGE = "确定要删除这个任务吗？删除后无法恢复。";
    public static final String CONFIRM_DELETE = "确认删除";
    public static final String CANCEL = "取消";
    public static final String TASK_DELETED = "任务已删除";
    
    // Fragment标签
    public static final String FRAGMENT_TASK_DETAIL = "taskDetail";
    public static final String FRAGMENT_ADD_TASK = "addTask";
    
    // Bundle参数
    public static final String ARG_ID = "arg_id";
    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_DESC = "arg_desc";
    public static final String ARG_TIME = "arg_time";
    public static final String ARG_START_TIME = "arg_start_time";
    
    // 默认值
    public static final int DEFAULT_SORT_INDEX = 0;
    public static final long DEFAULT_START_TIME = 0L;
    public static final boolean DEFAULT_COMPLETED = false;
    
    // 限制
    public static final int MAX_TITLE_LINES = 2;
    public static final int MAX_TITLE_WARN_COUNT = 2;
    public static final float SWIPE_THRESHOLD = 0.3f;
    public static final float SWIPE_ESCAPE_VELOCITY_MULTIPLIER = 0.5f;
}
