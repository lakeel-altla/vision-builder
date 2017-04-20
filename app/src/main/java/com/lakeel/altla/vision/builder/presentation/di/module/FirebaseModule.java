package com.lakeel.altla.vision.builder.presentation.di.module;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class FirebaseModule {

    @Singleton
    @Provides
    FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Singleton
    @Provides
    FirebaseStorage provideFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }
}
