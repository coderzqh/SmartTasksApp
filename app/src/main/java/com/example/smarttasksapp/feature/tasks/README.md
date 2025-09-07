# Tasks模块架构说明

## 📁 模块结构

```
SmartTasksApp/
├── core/                           # 核心框架层
│   ├── lifecycle/                  # 生命周期管理
│   │   ├── AppLifecycleManager.java    # 全局生命周期管理器
│   │   └── LifecycleScope.java         # 生命周期作用域
│   └── di/                        # 依赖注入
│       ├── DIContainer.java           # 依赖注入容器
│       ├── ModuleRegistry.java        # 模块注册器
│       └── Provider.java              # 提供者接口
├── feature/tasks/                  # 任务功能模块
│   ├── constants/
│   │   └── TaskConstants.java      # 常量管理
│   ├── data/
│   │   └── ITaskRepository.java    # 数据层接口（抽象）
│   ├── domain/                     # 领域层（纯业务逻辑）
│   │   └── usecase/
│   │       └── TaskUseCase.java    # 任务用例（业务逻辑）
│   └── ui/                         # 表现层
│       ├── adapter/
│       │   ├── TaskAdapter.java    # 任务列表适配器
│       │   └── SwipeToCompleteCallback.java  # 滑动操作回调
│       ├── utils/
│       │   └── DateTimePickerHelper.java      # 时间选择工具类
│       ├── view/
│       │   ├── AddTaskBottomSheet.java        # 添加任务弹窗
│       │   └── TaskDetailBottomSheet.java     # 任务详情弹窗
│       └── viewmodel/
│           └── TaskViewModel.java  # 任务视图模型
└── infrastructure/                 # 基础设施层
    ├── database/
    ├── dao/
    ├── entity/
    │   └── TaskEntity.java         # Infrastructure层Task实体
    └── repository/
        └── TaskRepositoryImpl.java # 实现feature层定义的接口
```

## 🏗️ 架构模式

采用 **Clean Architecture + MVVM + 依赖倒置** 架构模式：

### 分层架构
- **Core Layer**: 核心框架，提供生命周期管理和依赖注入
- **Feature Layer**: 功能模块，定义接口和业务逻辑
- **Infrastructure Layer**: 基础设施，实现具体的数据持久化

### 核心组件
- **AppLifecycleManager**: 全局生命周期管理器，负责应用启动、销毁和资源管理
- **LifecycleScope**: 生命周期作用域，管理特定作用域内的对象生命周期
- **DIContainer**: 依赖注入容器，管理接口到实现的映射和依赖解析
- **ModuleRegistry**: 模块注册器，负责自动发现和注册所有模块
- **TaskUseCase**: 任务用例，封装任务相关的业务逻辑

## 🔄 依赖倒置原则 (DIP)

### 1. **接口定义 (Feature层)**
```java
// feature/tasks/data/ITaskRepository.java
public interface ITaskRepository {
    LiveData<List<TaskEntity>> observeAll();
    CompletableFuture<Long> addTask(String title, String description, long startTime);
    CompletableFuture<Boolean> updateTask(TaskEntity task);
    CompletableFuture<Boolean> deleteTask(long taskId);
    CompletableFuture<Boolean> updateTaskCompletedStatus(long taskId, boolean isCompleted);
    CompletableFuture<Boolean> reorder(long fromTaskId, long toTaskId, boolean placeAbove);
    CompletableFuture<Boolean> persistOrder(List<TaskEntity> orderedTasks);
}
```

### 2. **接口实现 (Infrastructure层)**
```java
// infrastructure/repository/TaskRepositoryImpl.java
public class TaskRepositoryImpl implements ITaskRepository {
    // 实现所有接口方法
    // 负责数据持久化，不包含业务逻辑
}
```

### 3. **业务逻辑 (Feature层)**
```java
// feature/tasks/domain/usecase/TaskUseCase.java
public class TaskUseCase {
    private final ITaskRepository repository; // 依赖抽象，不依赖具体实现
    
    public TaskUseCase(ITaskRepository repository) {
        this.repository = repository;
    }
    
    // 封装业务逻辑，调用Repository接口
    // 使用TaskEntity进行数据操作
}
```

## 🚀 主要优势

### 1. **真正的依赖倒置**
- ✅ Feature层只定义接口，不依赖具体实现
- ✅ Infrastructure层实现接口，可以被轻松替换
- ✅ 通过依赖注入实现运行时绑定

### 2. **全局生命周期管理**
- ✅ 自动的依赖注入和资源清理
- ✅ 支持多种作用域（Application、Activity、Fragment、ViewModel）
- ✅ 防止内存泄漏

### 3. **完全解耦**
- ✅ Feature层和Infrastructure层完全分离
- ✅ 支持模块化开发和测试
- ✅ 可以独立部署和更新

### 4. **自动化和智能化**
- ✅ 模块自动发现和注册
- ✅ 依赖关系自动解析
- ✅ 循环依赖检测和预防

## 🔧 使用方式

### 1. **应用启动**
```java
public class SmartTasksApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 启动生命周期管理器
        AppLifecycleManager.getInstance().onApplicationStart(this);
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // 清理资源
        AppLifecycleManager.getInstance().onApplicationDestroy();
    }
}
```

### 2. **ViewModel使用**
```java
public class TaskViewModel extends AndroidViewModel {
    private final TaskUseCase taskUseCase;
    private final LifecycleScope scope;
    
    public TaskViewModel(@NonNull Application application) {
        super(application);
        
        // 通过生命周期管理器获取依赖
        AppLifecycleManager lifecycleManager = AppLifecycleManager.getInstance();
        
        // 创建ViewModel作用域
        this.scope = lifecycleManager.createScope("TaskViewModel_" + System.currentTimeMillis());
        
        // 获取依赖
        ITaskRepository repository = lifecycleManager.getDependency(ITaskRepository.class);
        this.taskUseCase = new TaskUseCase(repository);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        
        // 销毁作用域
        if (scope != null) {
            AppLifecycleManager.getInstance().destroyScope(scope.getName());
        }
    }
}
```

### 3. **业务逻辑调用**
```java
// 添加任务
public void addTask(String title, String description, long startTime) {
    taskUseCase.addTask(title, description, startTime)
        .thenAccept(taskId -> {
            // 处理成功
        })
        .exceptionally(throwable -> {
            // 处理错误
            return null;
        });
}
```

## 🔍 架构特点

### 1. **依赖注入**
- 自动注册接口实现
- 支持单例和作用域对象
- 自动依赖解析

### 2. **生命周期管理**
- 应用级生命周期管理
- 组件级作用域管理
- 自动资源清理

### 3. **模块化设计**
- 清晰的模块边界
- 松耦合的关系
- 可扩展的模块系统

### 4. **错误处理**
- 统一的异常处理
- 完整的错误日志
- 用户友好的错误提示

## 📈 性能优化

### 1. **异步操作**
- 使用CompletableFuture进行异步操作
- 避免阻塞主线程
- 支持操作链式调用

### 2. **内存管理**
- 自动的作用域管理
- 及时的资源释放
- 防止内存泄漏

### 3. **数据库优化**
- 使用Room进行数据访问
- 支持LiveData观察
- 批量操作支持

## 🤖 AI功能集成 (SiliconFlow API)

### 配置
AI模块已集成SiliconFlow API，用于生成任务描述和建议任务。

- **API地址**: https://api.siliconflow.cn/v1/chat/completions
- **模型**: Qwen/QwQ-32B
- **认证**: 使用Bearer Token认证

### 使用方式
AI功能通过依赖注入自动配置，开发者可以直接使用以下用例：

1. **GenerateTaskDescriptionUseCase**: 根据任务标题生成详细描述
2. **SuggestTasksUseCase**: 根据上下文建议相关任务

### 示例代码
```java
// 在ViewModel中使用AI功能
public class TaskViewModel extends AndroidViewModel {
    private final GenerateTaskDescriptionUseCase generateTaskDescriptionUseCase;
    private final SuggestTasksUseCase suggestTasksUseCase;
    
    public TaskViewModel(@NonNull Application application) {
        super(application);
        
        // 通过生命周期管理器获取AI用例
        AppLifecycleManager lifecycleManager = AppLifecycleManager.getInstance();
        this.generateTaskDescriptionUseCase = lifecycleManager.getDependency(GenerateTaskDescriptionUseCase.class);
        this.suggestTasksUseCase = lifecycleManager.getDependency(SuggestTasksUseCase.class);
    }
    
    // 生成任务描述
    public void generateTaskDescription(String taskTitle) {
        generateTaskDescriptionUseCase.execute(taskTitle)
            .thenAccept(description -> {
                // 更新UI显示生成的描述
            })
            .exceptionally(throwable -> {
                // 处理错误
                return null;
            });
    }
    
    // 建议任务
    public void suggestTasks(String context) {
        suggestTasksUseCase.execute(context)
            .thenAccept(suggestions -> {
                // 更新UI显示建议的任务
            })
            .exceptionally(throwable -> {
                // 处理错误
                return null;
            });
    }
}
```

这个新架构实现了真正的依赖倒置，让Feature层和Infrastructure层完全解耦，同时提供了全局的生命周期管理，使代码更加清晰、可维护和可测试。
