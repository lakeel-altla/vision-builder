package com.lakeel.altla.vision.builder.presentation.di.module;

import com.lakeel.altla.vision.builder.R;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ConfigModule {

    @Named(Names.GOOGLE_API_WEB_CLIENT_ID)
    @Singleton
    @Provides
    int provideGoogleApiWebClientId() {
        return R.string.default_web_client_id;
    }
}
