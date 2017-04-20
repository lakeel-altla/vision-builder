package com.lakeel.altla.vision.builder.presentation.di.module;

import com.lakeel.altla.vision.builder.presentation.app.MyApplication;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final MyApplication application;

    public ApplicationModule(@NonNull MyApplication application) {
        this.application = application;
    }

    @Named(Names.APPLICATION_CONTEXT)
    @Singleton
    @Provides
    Context provideContext() {
        return application;
    }

    @Singleton
    @Provides
    Resources provideResources() {
        return application.getResources();
    }
}
