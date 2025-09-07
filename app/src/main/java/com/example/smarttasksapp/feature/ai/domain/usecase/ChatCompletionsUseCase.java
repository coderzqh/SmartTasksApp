package com.example.smarttasksapp.feature.ai.domain.usecase;

import com.example.smarttasksapp.feature.ai.data.remote.model.request.ChatCompletionRequest;
import com.example.smarttasksapp.feature.ai.data.remote.model.request.Message;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;
import com.example.smarttasksapp.feature.ai.domain.repository.AiRepository;

import java.util.List;

public class ChatCompletionsUseCase {
    private final AiRepository repository;

    public ChatCompletionsUseCase(AiRepository repository) {
        this.repository = repository;
    }

    public ChatCompletionResponse execute(String model, List<Message> messages) {
        ChatCompletionRequest request = new ChatCompletionRequest(model, messages);
        return repository.chatCompletions(request);
    }
}