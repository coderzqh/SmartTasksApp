package com.example.smarttasksapp.feature.ai.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
        // 这里可以添加UI元素的初始化代码
        // 例如：绑定按钮点击事件、设置文本框等
        
        // 示例：显示一个简单的提示
        Toast.makeText(getContext(), "AI功能已启动", Toast.LENGTH_SHORT).show();
    }

    private void observeData() {
        // 观察ViewModel中的数据变化
        // 可以在这里添加对AI响应结果的监听
    }
}