package com.example.smarttasksapp.feature.ai.data.remote.model.request;

import java.util.List;

public class ChatCompletionRequest {
    private String model;
    private List<Message> messages;

    public ChatCompletionRequest() {
    }

    public ChatCompletionRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    // Getters and setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}