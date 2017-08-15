package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class ArModel {

    private static final Log LOG = LogFactory.getLog(ArModel.class);

    @NonNull
    private final VisionService visionService;

    @Nullable
    private AreaSettings areaSettings;

    @Nullable
    private Actor selectedActor;

    public ArModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @Nullable
    public synchronized AreaSettings getAreaSettings() {
        return areaSettings;
    }

    public synchronized void setAreaSettings(@Nullable AreaSettings areaSettings) {
        this.areaSettings = areaSettings;
    }

    @NonNull
    public synchronized AreaSettings getRequiredAreaSettings() {
        if (areaSettings == null) throw new IllegalStateException("'areaSettings' is null.");
        return areaSettings;
    }

    public synchronized boolean canEdit() {
        return areaSettings != null;
    }

    @Nullable
    public Actor getSelectedActor() {
        return selectedActor;
    }

    public void setSelectedActor(@Nullable Actor selectedActor) {
        this.selectedActor = selectedActor;

        if (selectedActor == null) {
            LOG.d("No actor is selected.");
        } else {
            LOG.d("An actor is selected.");
        }
    }

    @NonNull
    public Actor getRequiredSelectedActor() {
        if (selectedActor == null) throw new IllegalStateException("'selectedActor' is null.");
        return selectedActor;
    }

    @NonNull
    public synchronized TypedQuery<Actor> loadUserActors() {
        return visionService.getUserActorApi()
                            .findActorsByAreaId(getRequiredAreaSettings().getRequiredAreaId());
    }

    public void saveSelectedActor() {
        visionService.getUserActorApi()
                     .saveActor(getRequiredAreaSettings().getRequiredAreaId(), getRequiredSelectedActor());
    }

    public void deleteSelectedActor() {
        visionService.getUserActorApi()
                     .deleteActor(getRequiredAreaSettings().getRequiredAreaId(), getRequiredSelectedActor());
    }

    public void saveActor(@NonNull Actor actor) {
        visionService.getUserActorApi()
                     .saveActor(getRequiredAreaSettings().getRequiredAreaId(), actor);
    }
}
