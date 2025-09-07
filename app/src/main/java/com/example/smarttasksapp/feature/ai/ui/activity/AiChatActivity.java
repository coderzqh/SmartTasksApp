package com.example.smarttasksapp.feature.ai.ui.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarttasksapp.feature.ai.data.remote.model.request.Message;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;
import com.example.smarttasksapp.feature.ai.ui.viewmodel.AiViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AiChatActivity extends AppCompatActivity {
    private static final String TAG = "AiChatActivity";
    private AiViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_ai_chat);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AiViewModel.class);

        // Observe LiveData
        viewModel.getResponseLiveData().observe(this, this::handleResponse);
        viewModel.getErrorLiveData().observe(this, this::handleError);

        // Example usage
        sendExampleRequest();
    }

    private void sendExampleRequest() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", "What opportunities and challenges will the Chinese large model industry face in 2025?"));
        
        // Replace with the actual model name
        viewModel.chatCompletions("Qwen/QwQ-32B", messages);
    }

    private void handleResponse(ChatCompletionResponse response) {
        Log.d(TAG, "Received response: " + response);
        // Handle the response
    }

    private void handleError(String error) {
        Log.e(TAG, "Error: " + error);
        // Handle the error
    }
}