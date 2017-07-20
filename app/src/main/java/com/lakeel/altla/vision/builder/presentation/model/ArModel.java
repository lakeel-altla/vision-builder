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

    public synchronized void selectAreaSettings(@NonNull AreaSettings areaSettings) {
        this.areaSettings = areaSettings;
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
    public synchronized TypedQuery<Actor> loadUserActors() {
        if (areaSettings == null) throw new IllegalStateException("'areaSettings' is null.");

        final String areaId = areaSettings.getAreaId();

        if (areaId == null) throw new IllegalStateException("Unknown area id.");

        return visionService.getUserActorApi()
                            .findActorByAreaId(areaId);
    }

    public void saveSelectedActor() {
        if (selectedActor == null) throw new IllegalStateException("No actor is selected.");

        switch (selectedActor.getScopeAsEnum()) {
            case PUBLIC:
                // TODO
                break;
            case USER:
                visionService.getUserActorApi().saveActor(selectedActor);
                break;
            default:
                throw new IllegalArgumentException("An unexpected scope of an actor.");
        }
    }

    public void deleteSelectedActor() {
        if (selectedActor == null) throw new IllegalStateException("No actor is selected.");

        switch (selectedActor.getScopeAsEnum()) {
            case PUBLIC:
                // TODO
                break;
            case USER:
                visionService.getUserActorApi().deleteActor(selectedActor.getId());
                selectedActor = null;
                break;
            default:
                throw new IllegalArgumentException("An unexpected scope of an actor.");
        }
    }
}
