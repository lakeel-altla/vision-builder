package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Observable;

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
    public synchronized Observable<Actor> loadActors() {
        if (areaSettings == null) throw new IllegalStateException("'areaSettings' is null.");

        final String areaId = areaSettings.getAreaId();

        if (areaId == null) throw new IllegalStateException("Unknown area id.");

        if (areaSettings.getAreaScopeAsEnum() == Scope.PUBLIC) {
            return Observable.concat(e -> {
                visionService.getPublicActorApi()
                             .findActorsByAreaId(areaId,
                                                 actors -> {
                                                     for (Actor actor : actors) {
                                                         e.onNext(actor);
                                                     }
                                                     e.onComplete();
                                                 },
                                                 e::onError);
            }, e -> {
                visionService.getUserActorApi()
                             .findActorsByAreaId(areaId,
                                                 actors -> {
                                                     for (Actor actor : actors) {
                                                         e.onNext(actor);
                                                     }
                                                     e.onComplete();
                                                 },
                                                 e::onError);
            });
        } else {
            return Observable.create(e -> {
                visionService.getUserActorApi()
                             .findActorsByAreaId(areaId,
                                                 actors -> {
                                                     for (Actor actor : actors) {
                                                         e.onNext(actor);
                                                     }
                                                     e.onComplete();
                                                 },
                                                 e::onError);
            });
        }
    }
}
