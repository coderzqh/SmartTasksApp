package com.example.smarttasksapp.feature.ai.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import com.example.smarttasksapp.feature.ai.data.remote.model.request.Message;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttasksapp.R;
import com.example.smarttasksapp.feature.ai.ui.viewmodel.AiViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AiFragment extends Fragment {
    private AiViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(AiViewModel.class);
    }

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
        EditText etMessage = rootView.findViewById(R.id.et_message);
        ImageView ivCamera = rootView.findViewById(R.id.iv_camera);
        ImageView ivMicrophone = rootView.findViewById(R.id.iv_microphone);
        ImageView ivAdd = rootView.findViewById(R.id.iv_add);
        ImageView ivSend = rootView.findViewById(R.id.iv_send);
        
        // 设置相机按钮点击事件
        ivCamera.setOnClickListener(v -> {
            // 这里可以实现相机功能
            Toast.makeText(getContext(), "打开相机...", Toast.LENGTH_SHORT).show();
        });
        
        // 设置麦克风长按监听，实现"按住说话"功能
        ivMicrophone.setOnLongClickListener(v -> {
            // 这里可以实现录音功能
            Toast.makeText(getContext(), "开始录音...", Toast.LENGTH_SHORT).show();
            return true;
        });
        
        // 设置加号按钮点击事件
        ivAdd.setOnClickListener(v -> {
            // 这里可以实现添加附件等功能
            Toast.makeText(getContext(), "添加功能", Toast.LENGTH_SHORT).show();
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
        
        // 为输入框设置回车键发送消息
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    etMessage.setText("");
                }
                return true;
            }
            return false;
        });
        
        // 显示AI功能启动提示
        Toast.makeText(getContext(), "AI功能已启动", Toast.LENGTH_SHORT).show();
    }
    
    private void sendMessage(String message) {
        // 实现发送消息的逻辑
        Log.d("AiFragment", "发送消息: " + message);
        
        // 显示用户消息
        addMessageToChat(message, true);
        
        // 调用ViewModel发送消息到AI服务
        Message userMessage = new Message("user", message);
        viewModel.chatCompletions("deepseek-ai/DeepSeek-V3", java.util.Arrays.asList(userMessage));
    }

    private void observeData() {
        // 观察AI响应结果
        viewModel.getResponseLiveData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                try {
                    // 获取AI回复内容
                    String aiResponse = response.getChoices().get(0).getMessage().getContent();
                    if (aiResponse != null && !aiResponse.isEmpty()) {
                        // 显示AI回复
                        addMessageToChat(aiResponse, false);
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