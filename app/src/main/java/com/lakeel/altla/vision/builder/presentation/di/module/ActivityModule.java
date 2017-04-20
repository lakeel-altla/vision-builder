package com.lakeel.altla.vision.builder.presentation.di.module;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScope;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;

import org.greenrobot.eventbus.EventBus;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public final class ActivityModule {

    private final AppCompatActivity activity;

    public ActivityModule(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }

    @ActivityScope
    @Provides
    AppCompatActivity provideActivity() {
        return activity;
    }

    @Named(Names.ACTIVITY_CONTEXT)
    @ActivityScope
    @Provides
    Context provideContext() {
        return activity;
    }

    @ActivityScope
    @Provides
    ContentResolver provideContentResolver() {
        return activity.getContentResolver();
    }

    @ActivityScope
    @Provides
    VisionService provideVisionService(FirebaseDatabase firebaseDatabase, FirebaseStorage firebaseStorage) {
        return new VisionService(activity, firebaseDatabase, firebaseStorage);
    }

    @ActivityScope
    @Provides
    ArModel provideArModel() {
        return new ArModel();
    }

    @ActivityScope
    @Provides
    EventBus provideEventBus() {
        return new EventBus();
    }
}
