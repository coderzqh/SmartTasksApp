package com.example.smarttasksapp.feature.ai.data.remote.model.response;

public class Choice {
    private Message message;
    private String finish_reason;

    // Getters and setters
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getFinish_reason() {
        return finish_reason;
    }

    public void setFinish_reason(String finish_reason) {
        this.finish_reason = finish_reason;
    }
}