package com.example.smarttasksapp.feature.ai.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smarttasksapp.feature.ai.data.remote.model.request.Message;
import com.example.smarttasksapp.feature.ai.data.remote.model.response.ChatCompletionResponse;
import com.example.smarttasksapp.feature.ai.domain.usecase.ChatCompletionsUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AiViewModel extends ViewModel {
    private final ChatCompletionsUseCase useCase;
    private final MutableLiveData<ChatCompletionResponse> responseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @Inject
    public AiViewModel(ChatCompletionsUseCase useCase) {
        this.useCase = useCase;
    }

    public LiveData<ChatCompletionResponse> getResponseLiveData() {
        return responseLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void chatCompletions(String model, List<Message> messages) {
        new Thread(() -> {
            try {
                ChatCompletionResponse response = useCase.execute(model, messages);
                if (response != null) {
                    responseLiveData.postValue(response);
                } else {
                    errorLiveData.postValue("Failed to get response");
                }
            } catch (Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        }).start();
    }
}