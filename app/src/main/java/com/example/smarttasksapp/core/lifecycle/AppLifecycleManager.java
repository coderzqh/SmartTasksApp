package com.example.smarttasksapp.core.lifecycle;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.smarttasksapp.core.di.DIContainer;
import com.example.smarttasksapp.core.di.ModuleRegistry;
import com.example.smarttasksapp.core.resource.ResourceManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局生命周期管理器
 * 负责管理所有类的生命周期、依赖注入和资源清理
 */
public class AppLifecycleManager {
    private static final String TAG = "AppLifecycleManager";
    private static AppLifecycleManager instance;
    
    private final DIContainer diContainer;
    private final Map<String, LifecycleScope> scopes;
    private final Map<Class<?>, Object> singletons;
    private final ResourceManager resourceManager;
    private final ActivityLifecycleCallback activityLifecycleCallback;
    private Context applicationContext;
    private Application application;
    private boolean isInitialized = false;
    
    private AppLifecycleManager() {
        this.diContainer = new DIContainer();
        this.scopes = new ConcurrentHashMap<>();
        this.singletons = new ConcurrentHashMap<>();
        this.resourceManager = ResourceManager.getInstance();
        this.activityLifecycleCallback = new ActivityLifecycleCallback(this);
    }
    
    public static synchronized AppLifecycleManager getInstance() {
        if (instance == null) {
            instance = new AppLifecycleManager();
        }
        return instance;
    }
    
    /**
     * 应用启动时调用
     */
    public void onApplicationStart(Context context) {
        if (isInitialized) {
            Log.w(TAG, "AppLifecycleManager already initialized");
            return;
        }
        
        try {
            Log.d(TAG, "Initializing AppLifecycleManager");
            
            // 保存Application Context和Application实例
            this.applicationContext = context.getApplicationContext();
            if (context instanceof Application) {
                this.application = (Application) context;
                // 注册Activity生命周期回调
                this.application.registerActivityLifecycleCallbacks(activityLifecycleCallback);
            }
            
            // 将Context注册到DIContainer
            diContainer.registerSingleton(Context.class, this.applicationContext);
            
            // 自动发现和注册所有模块
            ModuleRegistry.autoDiscoverModules(diContainer);
            
            // 初始化基础设施
            initializeInfrastructure(context);
            
            isInitialized = true;
            Log.d(TAG, "AppLifecycleManager initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AppLifecycleManager", e);
            throw new RuntimeException("Failed to initialize application", e);
        }
    }
    
    /**
     * 应用销毁时调用
     */
    public void onApplicationDestroy() {
        Log.d(TAG, "Destroying AppLifecycleManager");
        
        // 注销Activity生命周期回调
        if (application != null) {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleCallback);
        }
        
        // 清理所有作用域
        scopes.values().forEach(LifecycleScope::destroy);
        scopes.clear();
        
        // 清理单例
        singletons.clear();
        
        // 清理所有资源
        resourceManager.cleanup();
        
        isInitialized = false;
        Log.d(TAG, "AppLifecycleManager destroyed");
    }
    
    /**
     * 注册模块
     */
    public void registerModule(Class<?> moduleClass) {
        diContainer.registerModule(moduleClass);
    }
    
    /**
     * 获取依赖
     */
    public <T> T getDependency(Class<T> type) {
        if (!isInitialized) {
            throw new IllegalStateException("AppLifecycleManager not initialized");
        }
        return diContainer.getDependency(type);
    }
    
    /**
     * 创建作用域
     */
    public LifecycleScope createScope(String scopeName) {
        LifecycleScope scope = new LifecycleScope(scopeName, diContainer);
        scopes.put(scopeName, scope);
        Log.d(TAG, "Created scope: " + scopeName);
        return scope;
    }
    
    /**
     * 销毁作用域
     */
    public void destroyScope(String scopeName) {
        LifecycleScope scope = scopes.remove(scopeName);
        if (scope != null) {
            scope.destroy();
            Log.d(TAG, "Destroyed scope: " + scopeName);
        }
    }
    
    /**
     * 注册单例
     */
    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
        Log.d(TAG, "Registered singleton: " + type.getSimpleName());
    }
    
    /**
     * 获取单例
     */
    @SuppressWarnings("unchecked")
    public <T> T getSingleton(Class<T> type) {
        return (T) singletons.get(type);
    }
    
    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * 初始化基础设施
     */
    private void initializeInfrastructure(Context context) {
        Log.d(TAG, "Initializing infrastructure");
        
        // 注册资源清理任务
        resourceManager.registerCleanupTask(() -> {
            Log.d(TAG, "Cleaning up infrastructure resources");
            // 这里可以添加数据库关闭等清理任务
        });
        
        Log.d(TAG, "Infrastructure initialized");
    }
    
    /**
     * 获取资源管理器
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    /**
     * 获取Activity生命周期回调
     */
    public ActivityLifecycleCallback getActivityLifecycleCallback() {
        return activityLifecycleCallback;
    }
}
