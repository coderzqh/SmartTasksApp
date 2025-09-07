package com.example.smarttasksapp.feature.ai.di;

import com.example.smarttasksapp.feature.ai.data.remote.api.AiServiceApi;
import com.example.smarttasksapp.feature.ai.data.repository.AiRepositoryImpl;
import com.example.smarttasksapp.feature.ai.domain.repository.AiRepository;
import com.example.smarttasksapp.feature.ai.domain.usecase.ChatCompletionsUseCase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AiModule {
    private static final String BASE_URL = "https://api.siliconflow.cn/";
    private static final String TOKEN = "sk-tjwtafrlkuyalaxyilrpvpvkjokdrzcobbrhhstudtbdnkvd";

    @Provides
    @Singleton
    public AiServiceApi provideAiServiceApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(AiServiceApi.class);
    }

    @Provides
    @Singleton
    public AiRepository provideAiRepository(AiServiceApi api) {
        return new AiRepositoryImpl(api, TOKEN);
    }

    @Provides
    @Singleton
    public ChatCompletionsUseCase provideChatCompletionsUseCase(AiRepository repository) {
        return new ChatCompletionsUseCase(repository);
    }
}