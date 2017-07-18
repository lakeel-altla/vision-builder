package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class ArModel {

    @NonNull
    private final VisionService visionService;

    private AreaSettings areaSettings;

    public ArModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @Nullable
    public synchronized AreaSettings getAreaSettings() {
        return areaSettings;
    }

    public synchronized void selectAreaSettings(@NonNull AreaSettings areaSettings) {
        this.areaSettings = areaSettings;
    }

    public synchronized boolean canEdit() {
        return areaSettings != null;
    }

    @NonNull
    public synchronized FirebaseQuery<Actor> loadUserActors() {
        if (areaSettings == null) throw new IllegalStateException("'areaSettings' is null.");

        final String areaId = areaSettings.getAreaId();

        if (areaId == null) throw new IllegalStateException("Unknown area id.");

        return visionService.getUserActorApi()
                            .findByAreaId(areaId);
    }
}
