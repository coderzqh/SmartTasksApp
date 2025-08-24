package com.example.smarttasksapp.core.util;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFuture工具类
 * 提供Android兼容的CompletableFuture方法
 */
public class CompletableFutureUtil {
    
    /**
     * 创建一个已完成的异常Future
     * Android兼容的failedFuture方法
     */
    public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }
    
    /**
     * 创建一个已完成的成功Future
     */
    public static <T> CompletableFuture<T> completedFuture(T value) {
        return CompletableFuture.completedFuture(value);
    }
}
