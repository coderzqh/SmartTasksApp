package com.example.smarttasksapp.core.di;

/**
 * 提供者接口
 * 用于延迟创建对象实例
 */
@FunctionalInterface
public interface Provider<T> {
    /**
     * 获取对象实例
     */
    T get();
}
