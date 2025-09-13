package com.example.smarttasksapp.feature.ai.data.repository;

import com.example.smarttasksapp.feature.ai.data.remote.api.AiServiceApi;
import com.example.smarttasksapp.feature.ai.data.remote.model.request.ChatCompletionRequest;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;
import com.example.smarttasksapp.feature.ai.domain.repository.AiRepository;

import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

public class AiRepositoryImpl implements AiRepository {
    private final AiServiceApi api;
    private final String token;

    public AiRepositoryImpl(AiServiceApi api, String token) {
        this.api = api;
        this.token = token;
    }

    @Override
    public ChatCompletionResponse chatCompletions(ChatCompletionRequest request) {        
        try {
            Call<ChatCompletionResponse> call = api.chatCompletions(
                    "Bearer " + token,
                    "application/json",
                    request
            );
            Response<ChatCompletionResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                // 处理错误响应，提供更具体的错误信息
                String errorMessage = "API Error: " + response.code() + " " + response.message();
                System.err.println(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (IOException e) {
            // 捕获并提供更具体的错误信息
            e.printStackTrace();
            String errorType = e instanceof java.net.SocketTimeoutException ? "请求超时" : "网络错误";
            throw new RuntimeException(errorType + ": " + e.getMessage(), e);
        }
    }
}