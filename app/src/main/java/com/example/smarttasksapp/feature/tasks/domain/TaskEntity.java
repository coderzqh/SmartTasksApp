package com.example.smarttasksapp.feature.tasks.domain;

import java.io.Serializable;

/**
 * 任务领域实体
 * 用于feature层内部使用，与infrastructure层解耦
 */
public class TaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private long id;
    private String title;
    private String description;
    private long createdAt;
    private long sortIndex;
    private boolean isCompleted;
    private long startTime;

    public TaskEntity() {}

    public TaskEntity(String title, String description, long startTime) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.createdAt = System.currentTimeMillis();
        this.isCompleted = false;
        this.sortIndex = 0;
    }

    public TaskEntity(long id, String title, String description, long createdAt,
                      long sortIndex, boolean isCompleted, long startTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.sortIndex = sortIndex;
        this.isCompleted = isCompleted;
        this.startTime = startTime;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getSortIndex() { return sortIndex; }
    public void setSortIndex(long sortIndex) { this.sortIndex = sortIndex; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskEntity task = (TaskEntity) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", sortIndex=" + sortIndex +
                ", isCompleted=" + isCompleted +
                ", startTime=" + startTime +
                '}';
    }
}
