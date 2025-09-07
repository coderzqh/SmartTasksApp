package com.example.smarttasksapp.feature.ai.domain.repository;

import com.example.smarttasksapp.feature.ai.data.remote.model.request.ChatCompletionRequest;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;

public interface AiRepository {
    ChatCompletionResponse chatCompletions(ChatCompletionRequest request);
}