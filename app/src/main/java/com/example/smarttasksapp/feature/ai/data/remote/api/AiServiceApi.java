package com.example.smarttasksapp.feature.ai.data.remote.api;

import com.example.smarttasksapp.feature.ai.data.remote.model.request.ChatCompletionRequest;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AiServiceApi {
    @POST("v1/chat/completions")
    Call<ChatCompletionResponse> chatCompletions(
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Body ChatCompletionRequest request
    );
}