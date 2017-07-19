package com.lakeel.altla.vision.builder.presentation.model;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.AreaDescriptionNameComparater;
import com.lakeel.altla.vision.helper.AreaNameComparater;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Single;

public final class SelectAreaSettingsModel {

    private final VisionService visionService;

    private final ArModel arModel;

    @NonNull
    private Scope areaScope = Scope.USER;

    @Nullable
    private Place place;

    @Nullable
    private String areaId;

    @Nullable
    private String areaName;

    @Nullable
    private String areaDescriptionId;

    @Nullable
    private String areaDescriptionName;

    @Nullable
    private AreaSettings areaSettings;

    public SelectAreaSettingsModel(@NonNull VisionService visionService, @NonNull ArModel arModel) {
        this.visionService = visionService;
        this.arModel = arModel;
    }

    @NonNull
    public Scope getAreaScope() {
        return areaScope;
    }

    @Nullable
    public String getAreaId() {
        return areaId;
    }

    @Nullable
    public String getAreaName() {
        return areaName;
    }

    @Nullable
    public String getAreaDescriptionId() {
        return areaDescriptionId;
    }

    @Nullable
    public String getAreaDescriptionName() {
        return areaDescriptionName;
    }

    @Nullable
    public AreaSettings getAreaSettings() {
        return areaSettings;
    }

    @NonNull
    public Observable<AreaSettings> loadAreaSettings() {
        return Single
                .<List<AreaSettings>>create(e -> {
                    visionService.getUserAreaSettingsApi()
                                 .findAllUserAreaSettings(e::onSuccess, e::onError);
                })
                .flatMapObservable(Observable::fromIterable);
    }

    @NonNull
    public Single<List<Area>> loadAreasByPlace() {
        if (place == null) throw new IllegalStateException("'place' is null.");

        final String placeId = place.getId();

        return Single.create(e -> {
            switch (areaScope) {
                case PUBLIC: {
                    visionService.getPublicAreaApi()
                                 .findAreasByPlaceId(placeId, areas -> {
                                     Collections.sort(areas, AreaNameComparater.INSTANCE);
                                     e.onSuccess(areas);
                                 }, e::onError);
                    break;
                }
                case USER: {
                    visionService.getUserAreaApi()
                                 .findAreasByPlaceId(placeId, areas -> {
                                     Collections.sort(areas, AreaNameComparater.INSTANCE);
                                     e.onSuccess(areas);
                                 }, e::onError);
                    break;
                }
            }
        });
    }

    @NonNull
    public Single<List<AreaDescription>> loadAreaDescriptionsByArea() {
        if (areaId == null) throw new IllegalStateException("'areaId' is null.");

        return Single.create(e -> {
            switch (areaScope) {
                case PUBLIC: {
                    visionService.getPublicAreaDescriptionApi()
                                 .findAreaDescriptionsByAreaId(areaId, areaDescriptions -> {
                                     Collections.sort(areaDescriptions, AreaDescriptionNameComparater.INSTANCE);
                                     e.onSuccess(areaDescriptions);
                                 }, e::onError);
                    break;
                }
                case USER: {
                    visionService.getUserAreaDescriptionApi()
                                 .findAreaDescriptionsByAreaId(areaId, areaDescriptions -> {
                                     Collections.sort(areaDescriptions, AreaDescriptionNameComparater.INSTANCE);
                                     e.onSuccess(areaDescriptions);
                                 }, e::onError);
                    break;
                }
            }
        });
    }

    public void selectAreaSettings(@NonNull AreaSettings areaSettings) {
        this.areaSettings = areaSettings;
    }

    public void selectAreaScope(@NonNull Scope areaScope) {
        if (this.areaScope != areaScope) {
            this.areaScope = areaScope;
            areaSettings = null;
            areaId = null;
            areaName = null;
            areaDescriptionId = null;
            areaDescriptionName = null;
        }
    }

    public void selectPlace(@NonNull Place place) {
        this.place = place;
    }

    public void selectArea(@NonNull Area area) {
        if (!Objects.equals(areaId, area.getId())) {
            areaId = area.getId();
            areaName = area.getName();
            areaSettings = null;
            areaDescriptionId = null;
            areaDescriptionName = null;
        }
    }

    public void selectAreaDescription(@NonNull AreaDescription areaDescription) {
        if (!Objects.equals(areaDescriptionId, areaDescription.getId())) {
            areaDescriptionId = areaDescription.getId();
            areaDescriptionName = areaDescription.getName();
            areaSettings = null;
        }
    }

    public void start() {
        if (!canStart()) throw new IllegalStateException("Can not start.");

        if (areaSettings == null) {
            areaSettings = new AreaSettings();
            areaSettings.setUserId(CurrentUser.getInstance().getUserId());
            areaSettings.setAreaScopeAsEnum(areaScope);
            areaSettings.setAreaId(areaId);
            areaSettings.setAreaName(areaName);
            areaSettings.setAreaDescriptionId(areaDescriptionId);
            areaSettings.setAreaDescriptionName(areaDescriptionName);
        }

        visionService.getUserAreaSettingsApi()
                     .saveUserAreaSettings(areaSettings);

        arModel.selectAreaSettings(areaSettings);
    }

    public boolean canStart() {
        return areaId != null && areaDescriptionId != null;
    }
}
