package com.lakeel.altla.vision.builder.presentation.di.module;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;

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

    @Singleton
    @Provides
    VisionService provideVisionService(FirebaseDatabase firebaseDatabase, FirebaseStorage firebaseStorage) {
        return new VisionService(application, firebaseDatabase, firebaseStorage);
    }

    @Singleton
    @Provides
    ArModel provideArModel(VisionService visionService) {
        return new ArModel(visionService);
    }
}
