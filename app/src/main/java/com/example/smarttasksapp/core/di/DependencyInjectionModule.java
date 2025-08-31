package com.example.smarttasksapp.core.di;

import android.content.Context;

import com.example.smarttasksapp.feature.tasks.data.ITaskRepository;
import com.example.smarttasksapp.infrastructure.database.AppDatabase;
import com.example.smarttasksapp.infrastructure.repository.TaskRepositoryImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DependencyInjectionModule {
    
    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }
    
    @Provides
    @Singleton
    public ITaskRepository provideTaskRepository(@ApplicationContext Context context) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        return new TaskRepositoryImpl(context, executorService);
    }
}