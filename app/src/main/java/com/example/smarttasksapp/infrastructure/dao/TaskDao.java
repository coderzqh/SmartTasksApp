package com.example.smarttasksapp.infrastructure.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Update
    void update(Task task);

    @Query("SELECT * FROM tasks ORDER BY sortIndex DESC, createdAt ASC")
    LiveData<List<Task>> observeAll();

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY sortIndex DESC, createdAt ASC")
    LiveData<List<Task>> observeByCompletionStatus(boolean isCompleted);

    @Query("SELECT COALESCE(MAX(sortIndex), 0) FROM tasks")
    long getMaxSortIndex();

    @Query("UPDATE tasks SET sortIndex = :sortIndex WHERE id = :taskId")
    void updateSortIndex(long taskId, long sortIndex);

    @Query("UPDATE tasks SET title = :title, description = :description WHERE id = :taskId")
    void updateTitleAndDescription(long taskId, String title, String description);
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    void updateCompletedStatus(long taskId, boolean isCompleted);
    
    @Query("UPDATE tasks SET startTime = :startTime WHERE id = :taskId")
    void updateStartTime(long taskId, long startTime);
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTask(long taskId);

    // 批量更新排序索引
    @Transaction
    default void updateSortIndices(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            long sortIndex = tasks.size() - i; // 顶部任务有更大的sortIndex
            updateSortIndex(task.getId(), sortIndex);
        }
    }

    // 获取任务数量
    @Query("SELECT COUNT(*) FROM tasks")
    int getTaskCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = :isCompleted")
    int getTaskCountByStatus(boolean isCompleted);
}


