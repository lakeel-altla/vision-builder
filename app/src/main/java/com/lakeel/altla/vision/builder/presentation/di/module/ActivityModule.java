package com.lakeel.altla.vision.builder.presentation.di.module;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScope;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.builder.presentation.model.AreaDescriptionListByAreaModel;
import com.lakeel.altla.vision.builder.presentation.model.AreaListByPlaceModel;
import com.lakeel.altla.vision.builder.presentation.model.AreaSettingsListModel;
import com.lakeel.altla.vision.builder.presentation.model.AreaSettingsModel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public final class ActivityModule {

    private final Activity activity;

    public ActivityModule(@NonNull Activity activity) {
        this.activity = activity;
    }

    @ActivityScope
    @Provides
    Activity provideActivity() {
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
    AreaSettingsModel provideSelectAreaSettingsModel(VisionService visionService, ArModel arModel) {
        return new AreaSettingsModel(visionService, arModel);
    }

    @ActivityScope
    @Provides
    AreaSettingsListModel provideAreaSettingsListModel(VisionService visionService) {
        return new AreaSettingsListModel(visionService);
    }

    @ActivityScope
    @Provides
    AreaListByPlaceModel provideAreaListByPlaceModel(VisionService visionService) {
        return new AreaListByPlaceModel(visionService);
    }

    @ActivityScope
    @Provides
    AreaDescriptionListByAreaModel provideAreaDescriptionListByAreaModel(VisionService visionService) {
        return new AreaDescriptionListByAreaModel(visionService);
    }
}
