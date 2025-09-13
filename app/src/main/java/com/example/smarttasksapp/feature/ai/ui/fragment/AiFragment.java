package com.example.smarttasksapp.feature.ai.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.smarttasksapp.R;
import com.example.smarttasksapp.feature.ai.data.remote.model.request.Message;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;
import com.example.smarttasksapp.feature.ai.ui.viewmodel.AiViewModel;
import com.example.smarttasksapp.feature.input.AliyunOcrManager;
import com.example.smarttasksapp.feature.input.OcrResult;
import com.example.smarttasksapp.feature.tasks.domain.TaskEntity;
import com.example.smarttasksapp.feature.tasks.ui.viewmodel.TaskViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AndroidEntryPoint
public class AiFragment extends Fragment {
    private static final String TAG = "AiFragment";
    private static final int REQUEST_CODE_SELECT_IMAGE = 1001;
    private static final int REQUEST_CODE_PERMISSIONS = 1002;
    private static final int REQUEST_CODE_CAMERA = 1003;

    private AiViewModel viewModel;
    private TaskViewModel taskViewModel;
    private AliyunOcrManager ocrManager;
    private EditText etMessage;
    private String PRE_MESSAGE = "请根据下面的文字分析出能够一步一步执行的任务,每个任务包含标题和描述，标题不超过30个字，描述不超过100个字，" +
            "并且返回信息中只有json字符串，" +
            "json格式" +
            "{" +
            "\"tasks\":[" +
                "{\"title\":\"\"," +
                "\"description\":\"\"}," +
                "{\"title\":\"\"," +
                "\"description\":\"\"}" +
            "]" +
            "}。\\n";

    // 存储任务卡片与任务实体的映射关系
    private Map<View, TaskEntity> taskViewMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(AiViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // 初始化OCR管理器
        if (getActivity() != null) {
            ocrManager = new AliyunOcrManager(getActivity());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ai, container, false);
        
        // 初始化UI元素和事件监听器
        initUI(rootView);
        
        // 设置数据观察者
        observeData();
        
        return rootView;
    }

    private void initUI(View rootView) {
        // 初始化UI元素
        etMessage = rootView.findViewById(R.id.et_message);
        ImageView ivCamera = rootView.findViewById(R.id.iv_camera);
        ImageView ivMicrophone = rootView.findViewById(R.id.iv_microphone);
        ImageView ivAdd = rootView.findViewById(R.id.iv_add);
        ImageView ivSend = rootView.findViewById(R.id.iv_send);

        // 设置相机按钮点击事件
        ivCamera.setOnClickListener(v -> {
            checkCameraPermission();
        });

        // 设置麦克风长按监听，实现"按住说话"功能
        ivMicrophone.setOnLongClickListener(v -> {
            // 这里可以实现录音功能
            Toast.makeText(getContext(), "开始录音...", Toast.LENGTH_SHORT).show();
            return true;
        });

        // 设置加号按钮点击事件
        ivAdd.setOnClickListener(v -> {
            // 检查权限并打开图片选择器
            checkPermissionsAndSelectImage();
        });

        // 设置发送按钮点击事件
        ivSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                etMessage.setText("");
            }
        });

        // 为输入框设置TextWatcher，根据输入内容切换UI状态
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 不需要实现
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 不需要实现
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 根据输入内容是否为空来切换UI状态
                if (s.toString().trim().isEmpty()) {
                    // 输入框为空，显示所有图标，隐藏发送按钮
                    ivCamera.setVisibility(View.VISIBLE);
                    ivMicrophone.setVisibility(View.VISIBLE);
                    ivAdd.setVisibility(View.VISIBLE);
                    ivSend.setVisibility(View.GONE);
                } else {
                    // 输入框有内容，隐藏其他图标，显示发送按钮
                    ivCamera.setVisibility(View.GONE);
                    ivMicrophone.setVisibility(View.GONE);
                    ivAdd.setVisibility(View.GONE);
                    ivSend.setVisibility(View.VISIBLE);
                }
            }
        });
        
        // 移除回车键发送消息功能，只允许通过点击发送按钮发送消息
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            return false;
        });

        // 显示AI功能启动提示
        Toast.makeText(getContext(), "AI功能已启动", Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查权限并选择图片
     */
    private void checkPermissionsAndSelectImage() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSIONS);
        } else {
            selectImage();
        }
    }

    /**
     * 检查相机权限
     */
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSIONS);
        } else {
            openCamera();
        }
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
        } else {
            Toast.makeText(getContext(), "没有可用的相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择图片
     */
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    /**
     * 处理图片选择结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_SELECT_IMAGE) {
                // 处理从相册选择的图片
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        // 从Uri加载图片
                        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // 显示加载中提示
                        Toast.makeText(getContext(), "正在识别图片中的文字...", Toast.LENGTH_SHORT).show();

                        // 使用阿里云OCR识别文字
                        recognizeTextFromImage(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "加载图片失败", e);
                        Toast.makeText(getContext(), "加载图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == REQUEST_CODE_CAMERA) {
                // 处理相机拍摄的照片
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap bitmap = (Bitmap) extras.get("data");
                    if (bitmap != null) {
                        // 显示加载中提示
                        Toast.makeText(getContext(), "正在识别图片中的文字...", Toast.LENGTH_SHORT).show();

                        // 使用阿里云OCR识别文字
                        recognizeTextFromImage(bitmap);
                    }
                }
            }
        }
    }

    /**
     * 从图片中识别文字
     */
    private void recognizeTextFromImage(Bitmap bitmap) {
        if (ocrManager == null) {
            Toast.makeText(getContext(), "OCR管理器初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用阿里云OCR识别文字
        CompletableFuture<OcrResult> future = ocrManager.recognizeText(bitmap);
        future.thenAccept(result -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (result != null) {
                        // 获取识别的文字
                        StringBuilder recognizedText = new StringBuilder();
                        if (result.getContent() != null) {
                            recognizedText.append(result.getContent());

                        }

                        // 如果没有识别到文字但有错误信息
                        if ((recognizedText.length() == 0 || result.getContent().startsWith("识别失败"))){
                            Toast.makeText(getContext(), result.getContent(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, result.getContent());
                        } else {

                            // 将识别的文字显示在输入框中
                            String textResult = recognizedText.toString().trim();
                            if (!textResult.isEmpty()) {
                                etMessage.setText(textResult);
                                etMessage.setSelection(textResult.length());
                            }
                            // 显示识别成功提示
                            Toast.makeText(getContext(), "文字识别成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).exceptionally(throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // 显示错误信息
                    Toast.makeText(getContext(), "识别失败: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "OCR识别失败", throwable);
                });
            }
            return null;
        });
    }

    /**
     * 解析OCR识别结果
     */
    private String parseOcrResult(String jsonResult) {
        try {
            JsonObject jsonObject = new Gson().fromJson(jsonResult, JsonObject.class);
            if (jsonObject.has("body")) {
                JsonObject body = jsonObject.getAsJsonObject("body");
                if (body.has("prism_wordsInfo")) {
                    StringBuilder textBuilder = new StringBuilder();
                    for (JsonElement element : body.getAsJsonArray("prism_wordsInfo")) {
                        if (element.isJsonObject() && element.getAsJsonObject().has("word")) {
                            textBuilder.append(element.getAsJsonObject().get("word").getAsString());
                            textBuilder.append("\n");
                        }
                    }
                    return textBuilder.toString().trim();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析OCR结果失败", e);
        }
        return jsonResult; // 如果解析失败，返回原始结果
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 判断请求的是哪种权限
                if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    selectImage();
                } else if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    openCamera();
                }
            } else {
                // 根据请求的权限类型显示不同的提示
                if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(getContext(), "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                } else if (permissions[0].equals(Manifest.permission.CAMERA)) {
                    Toast.makeText(getContext(), "需要相机权限才能拍照", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void sendMessage(String message) {
        // 实现发送消息的逻辑
        Log.d("AiFragment", "发送消息: " + message);

        // 显示用户消息
        addMessageToChat(message, true);

        // 调用ViewModel发送消息到AI服务
        Message userMessage = new Message("user", PRE_MESSAGE+message);
        viewModel.chatCompletions("deepseek-ai/DeepSeek-V3", java.util.Arrays.asList(userMessage));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void observeData() {
        // 观察AI响应结果
        viewModel.getResponseLiveData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                try {
                    // 获取AI回复内容
                    String aiResponse = response.getChoices().get(0).getMessage().getContent();
                    if (aiResponse != null && !aiResponse.isEmpty()) {
                        // 尝试解析为任务JSON
                        if (isTasksJson(aiResponse)) {
                            List<TaskEntity> taskEntities = parseTasksJson(aiResponse);
                            if (taskEntities != null && !taskEntities.isEmpty()) {
                                // 显示任务卡片
                                addTasksToChat(taskEntities);
                            } else {
                                // 解析失败，显示原始文本
                                addMessageToChat(aiResponse, false);
                            }
                        } else {
                            // 不是任务JSON，显示原始文本
                            addMessageToChat(aiResponse, false);
                        }
                    }
                } catch (Exception e) {
                    Log.e("AiFragment", "解析AI响应失败: " + e.getMessage());
                    Toast.makeText(getContext(), "解析AI响应失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("AiFragment", "未收到有效响应");
                Toast.makeText(getContext(), "未收到有效响应", Toast.LENGTH_SHORT).show();
            }
        });

        // 观察错误信息
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Log.e("AiFragment", "AI请求错误: " + errorMessage);
                Toast.makeText(getContext(), "错误: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 观察任务添加结果
        taskViewModel.getIsOperationSuccessful().observe(getViewLifecycleOwner(), isSuccessful -> {
            if (isSuccessful != null && isSuccessful) {
                Toast.makeText(getContext(), "任务添加成功", Toast.LENGTH_SHORT).show();
                taskViewModel.clearSuccess();
            }
        });

        // 观察任务添加错误
        taskViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 判断字符串是否为任务JSON格式
     */
    private boolean isTasksJson(String text) {
        try {
            // 查找第一个换行符并获取其后面的字符串
            int firstLineBreakIndex = text.indexOf('\n');
            String contentAfterFirstLineBreak = firstLineBreakIndex != -1 ? text.substring(firstLineBreakIndex + 1) : text;

            // 尝试清理文本，移除可能的Markdown标记
            String cleanedText = contentAfterFirstLineBreak.trim();
            int endIndex = cleanedText.lastIndexOf("```");
            cleanedText = cleanedText.substring(0, endIndex).trim();
            Log.d(TAG, cleanedText);
            // 检查是否包含tasks数组
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析任务JSON字符串为TaskEntity列表
     */
    private List<TaskEntity> parseTasksJson(String jsonText) {
        try {
            // 尝试清理文本，移除可能的Markdown标记
            int firstLineBreakIndex = jsonText.indexOf('\n');
            String contentAfterFirstLineBreak = firstLineBreakIndex != -1 ? jsonText.substring(firstLineBreakIndex + 1) : jsonText;

            // 尝试清理文本，移除可能的Markdown标记
            String cleanedText = contentAfterFirstLineBreak.trim();
            int endIndex = cleanedText.lastIndexOf("```");
            cleanedText = cleanedText.substring(0, endIndex).trim();

            // 解析JSON
            Gson gson = new Gson();
            Type taskListType = new TypeToken<Map<String, List<Map<String, String>>>>(){}.getType();
            Map<String, List<Map<String, String>>> jsonMap = gson.fromJson(cleanedText, taskListType);

            if (jsonMap != null && jsonMap.containsKey("tasks")) {
                List<Map<String, String>> tasksList = jsonMap.get("tasks");
                List<TaskEntity> taskEntities = new ArrayList<>();

                long startTime = System.currentTimeMillis() + 3600000; // 默认设置为1小时后

                for (Map<String, String> taskMap : tasksList) {
                    String title = taskMap.getOrDefault("title", "").trim();
                    String description = taskMap.getOrDefault("description", "").trim();

                    if (!title.isEmpty()) {
                        TaskEntity taskEntity = new TaskEntity(title, description, startTime);
                        taskEntities.add(taskEntity);
                        startTime += 3600000; // 每个任务间隔1小时
                    }
                }

                return taskEntities;
            }
        } catch (Exception e) {
            Log.e(TAG, "解析任务JSON失败: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将任务列表添加到聊天界面
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void addTasksToChat(List<TaskEntity> tasks) {
        View rootView = getView();
        if (rootView == null || getContext() == null) return;

        LinearLayout chatContainer = rootView.findViewById(R.id.chat_container);
        ScrollView scrollView = rootView.findViewById(R.id.content_scroll);

        // 创建标题视图
        TextView titleView = new TextView(getContext());
        titleView.setText("任务建议：");
        titleView.setTextSize(16);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(8, 16, 8, 8);
        titleView.setLayoutParams(titleParams);
        chatContainer.addView(titleView);

        // 为每个任务创建一个卡片
        for (TaskEntity taskEntity : tasks) {
            addTaskCardToChat(taskEntity);
        }

        // 滚动到底部
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    /**
     * 将单个任务卡片添加到聊天界面
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void addTaskCardToChat(TaskEntity taskEntity) {
        View rootView = getView();
        if (rootView == null || getContext() == null) return;

        LinearLayout chatContainer = rootView.findViewById(R.id.chat_container);
        ScrollView scrollView = rootView.findViewById(R.id.content_scroll);

        // 加载任务卡片布局
        View taskCardView = LayoutInflater.from(getContext()).inflate(R.layout.task_card, null);
        EditText taskTitle = taskCardView.findViewById(R.id.task_title);
        EditText taskDescription = taskCardView.findViewById(R.id.task_description);
        Button addToTaskBtn = taskCardView.findViewById(R.id.add_to_task_btn);

        // 设置任务数据
        taskTitle.setText(taskEntity.getTitle());
        taskDescription.setText(taskEntity.getDescription());

        // 保存任务实体与视图的映射关系
        taskViewMap.put(taskCardView, taskEntity);

        // 设置添加任务按钮点击事件
        addToTaskBtn.setOnClickListener(v -> {
            // 更新任务实体的数据
            taskEntity.setTitle(taskTitle.getText().toString().trim());
            taskEntity.setDescription(taskDescription.getText().toString().trim());

            // 添加任务到数据库
            addTaskToDatabase(taskEntity);

            // 立即更新按钮状态为已添加并禁用
            addToTaskBtn.setText("已添加");
            addToTaskBtn.setEnabled(false);
            addToTaskBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        });

        // 设置布局参数
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 4, 8, 4);
        taskCardView.setLayoutParams(layoutParams);

        // 添加到聊天容器
        chatContainer.addView(taskCardView);

        // 滚动到底部
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    /**
     * 将任务添加到数据库
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void addTaskToDatabase(TaskEntity taskEntity) {
        if (taskEntity == null || taskEntity.getTitle().trim().isEmpty()) {
            Toast.makeText(getContext(), "任务标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用TaskViewModel添加任务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            taskViewModel.addTask(
                    taskEntity.getTitle().trim(),
                    taskEntity.getDescription(),
                    taskEntity.getStartTime()
            );
        } else {
            // 对于Android 12以下的版本，使用当前时间作为开始时间
            taskViewModel.addTask(
                    taskEntity.getTitle().trim(),
                    taskEntity.getDescription(),
                    System.currentTimeMillis()
            );
        }
    }

    private void addMessageToChat(String message, boolean isUser) {
        View rootView = getView();
        if (rootView == null || getContext() == null) return;

        LinearLayout chatContainer = rootView.findViewById(R.id.chat_container);
        ScrollView scrollView = rootView.findViewById(R.id.content_scroll);

        // 创建消息卡片
        View messageView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, null);
        TextView messageText = messageView.findViewById(R.id.message_text);
        messageText.setText(message);

        // 使用固定的最大宽度，避免布局未加载时宽度计算问题
        int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.75);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 8, 8, 8);

        // 设置消息气泡样式
        LinearLayout messageBubble = messageView.findViewById(R.id.message_bubble);
        if (isUser) {
            // 用户消息靠右显示
            layoutParams.gravity = Gravity.END;
            messageBubble.setBackgroundResource(R.drawable.user_message_bg);
            messageText.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            // AI消息靠左显示
            layoutParams.gravity = Gravity.START;
            messageBubble.setBackgroundResource(R.drawable.ai_message_bg);
            messageText.setTextColor(getResources().getColor(android.R.color.black));
        }

        // 设置最大宽度
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bubbleParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        bubbleParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        bubbleParams.weight = 0;
        bubbleParams.width = Math.min(LinearLayout.LayoutParams.WRAP_CONTENT, maxWidth);
        messageBubble.setLayoutParams(bubbleParams);

        messageView.setLayoutParams(layoutParams);
        chatContainer.addView(messageView);

        // 滚动到底部
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}