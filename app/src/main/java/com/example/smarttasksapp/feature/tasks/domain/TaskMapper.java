package com.example.smarttasksapp.feature.tasks.domain;

import com.example.smarttasksapp.infrastructure.entity.Task;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务映射器
 * 负责Domain层和Infrastructure层Task实体之间的转换
 */
public class TaskMapper {
    
    /**
     * 将Infrastructure层的Task转换为Domain层的Task
     */
    public static com.example.smarttasksapp.feature.tasks.domain.Task toDomain(Task infrastructureTask) {
        if (infrastructureTask == null) {
            return null;
        }
        
        return new com.example.smarttasksapp.feature.tasks.domain.Task(
                infrastructureTask.getId(),
                infrastructureTask.getTitle(),
                infrastructureTask.getDescription(),
                infrastructureTask.getCreatedAt(),
                infrastructureTask.getSortIndex(),
                infrastructureTask.isCompleted(),
                infrastructureTask.getStartTime()
        );
    }
    
    /**
     * 将Domain层的Task转换为Infrastructure层的Task
     */
    public static Task toInfrastructure(com.example.smarttasksapp.feature.tasks.domain.Task domainTask) {
        if (domainTask == null) {
            return null;
        }
        
        Task infrastructureTask = new Task();
        infrastructureTask.setId(domainTask.getId());
        infrastructureTask.setTitle(domainTask.getTitle());
        infrastructureTask.setDescription(domainTask.getDescription());
        infrastructureTask.setCreatedAt(domainTask.getCreatedAt());
        infrastructureTask.setSortIndex(domainTask.getSortIndex());
        infrastructureTask.setCompleted(domainTask.isCompleted());
        infrastructureTask.setStartTime(domainTask.getStartTime());
        
        return infrastructureTask;
    }
    
    /**
     * 将Infrastructure层的Task列表转换为Domain层的Task列表
     */
    public static List<com.example.smarttasksapp.feature.tasks.domain.Task> toDomainList(List<Task> infrastructureTasks) {
        if (infrastructureTasks == null) {
            return null;
        }
        
        return infrastructureTasks.stream()
                .map(TaskMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * 将Domain层的Task列表转换为Infrastructure层的Task列表
     */
    public static List<Task> toInfrastructureList(List<com.example.smarttasksapp.feature.tasks.domain.Task> domainTasks) {
        if (domainTasks == null) {
            return null;
        }
        
        return domainTasks.stream()
                .map(TaskMapper::toInfrastructure)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建新的Domain层Task（用于添加操作）
     */
    public static com.example.smarttasksapp.feature.tasks.domain.Task createDomainTask(String title, String description, long startTime) {
        return new com.example.smarttasksapp.feature.tasks.domain.Task(
                title, 
                description, 
                System.currentTimeMillis()
        );
    }
}
