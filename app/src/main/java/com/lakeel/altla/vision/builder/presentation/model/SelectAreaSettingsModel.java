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
    private Area area;

    @Nullable
    private AreaDescription areaDescription;

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
    public Area getArea() {
        return area;
    }

    @Nullable
    public AreaDescription getAreaDescription() {
        return areaDescription;
    }

    @Nullable
    public AreaSettings getAreaSettings() {
        return areaSettings;
    }

    @NonNull
    public Observable<AreaSettingsDetail> loadAreaSettingsDetails() {
        return Single
                .<List<AreaSettings>>create(e -> {
                    visionService.getUserAreaSettingsApi()
                                 .findAllUserAreaSettings(e::onSuccess, e::onError);
                })
                .flatMapObservable(Observable::fromIterable)
                .map(AreaSettingsDetail::new)
                .concatMap(detail -> Observable.<AreaSettingsDetail>create(e -> {
                    String areaId = detail.areaSettings.getAreaId();
                    if (areaId == null) {
                        throw new IllegalStateException("Field 'areaId' is null.");
                    }

                    switch (detail.areaSettings.getAreaScopeAsEnum()) {
                        case PUBLIC:
                            visionService.getPublicAreaApi()
                                         .findAreaById(areaId, area -> {
                                             if (area != null) {
                                                 detail.area = area;
                                                 e.onNext(detail);
                                             }
                                             e.onComplete();
                                         }, e::onError);
                            break;
                        case USER:
                            visionService.getUserAreaApi()
                                         .findAreaById(areaId, area -> {
                                             if (area != null) {
                                                 detail.area = area;
                                                 e.onNext(detail);
                                             }
                                             e.onComplete();
                                         }, e::onError);
                            break;
                        default:
                            throw new IllegalStateException("Unknown area scope.");
                    }
                }))
                .concatMap(detail -> Observable.<AreaSettingsDetail>create(e -> {
                    String areaDescriptionId = detail.areaSettings.getAreaDescriptionId();
                    if (areaDescriptionId == null) {
                        throw new IllegalStateException("Field 'areaId' is null.");
                    }

                    switch (detail.areaSettings.getAreaScopeAsEnum()) {
                        case PUBLIC:
                            visionService.getPublicAreaDescriptionApi()
                                         .findAreaDescriptionById(areaDescriptionId, areaDescription -> {
                                             if (areaDescription != null) {
                                                 detail.areaDescription = areaDescription;
                                                 e.onNext(detail);
                                             }
                                             e.onComplete();
                                         }, e::onError);
                            break;
                        case USER:
                            visionService.getUserAreaDescriptionApi()
                                         .findAreaDescriptionById(areaDescriptionId, areaDescription -> {
                                             if (areaDescription != null) {
                                                 detail.areaDescription = areaDescription;
                                                 e.onNext(detail);
                                             }
                                             e.onComplete();
                                         }, e::onError);
                            break;
                        default:
                            throw new IllegalStateException("Unknown area scope.");
                    }
                }));
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
        if (area == null) throw new IllegalStateException("'area' is null.");

        return Single.create(e -> {
            switch (areaScope) {
                case PUBLIC: {
                    visionService.getPublicAreaDescriptionApi()
                                 .findAreaDescriptionsByAreaId(area.getId(), areaDescriptions -> {
                                     Collections.sort(areaDescriptions, AreaDescriptionNameComparater.INSTANCE);
                                     e.onSuccess(areaDescriptions);
                                 }, e::onError);
                    break;
                }
                case USER: {
                    visionService.getUserAreaDescriptionApi()
                                 .findAreaDescriptionsByAreaId(area.getId(), areaDescriptions -> {
                                     Collections.sort(areaDescriptions, AreaDescriptionNameComparater.INSTANCE);
                                     e.onSuccess(areaDescriptions);
                                 }, e::onError);
                    break;
                }
            }
        });
    }

    public void selectAreaSettings(@NonNull AreaSettings areaSettings, @NonNull Area area,
                                   @NonNull AreaDescription areaDescription) {
        this.areaSettings = areaSettings;
        areaScope = areaSettings.getAreaScopeAsEnum();
        this.area = area;
        this.areaDescription = areaDescription;
    }

    public void selectAreaScope(@NonNull Scope areaScope) {
        if (this.areaScope != areaScope) {
            this.areaScope = areaScope;
            areaSettings = null;
            area = null;
            areaDescription = null;
        }
    }

    public void selectPlace(@NonNull Place place) {
        this.place = place;
    }

    public void selectArea(@NonNull Area area) {
        if (!Objects.equals(this.area, area)) {
            this.area = area;
            areaSettings = null;
            areaDescription = null;
        }
    }

    public void selectAreaDescriptiob(@NonNull AreaDescription areaDescription) {
        if (!Objects.equals(this.areaDescription, areaDescription)) {
            this.areaDescription = areaDescription;
            areaSettings = null;
        }
    }

    public void start() {
        if (!canStart()) throw new IllegalStateException("Can not start.");

        AreaSettings areaSettings = this.areaSettings;
        if (areaSettings == null) {
            areaSettings = new AreaSettings();
            areaSettings.setUserId(CurrentUser.getInstance().getUserId());
        }

        areaSettings.setAreaScopeAsEnum(areaScope);
        areaSettings.setAreaId(area.getId());
        areaSettings.setAreaDescriptionId(areaDescription.getId());

        visionService.getUserAreaSettingsApi()
                     .saveUserAreaSettings(areaSettings);

        arModel.selectAreaSettings(areaSettings);
    }

    public boolean canStart() {
        return area != null && areaDescription != null;
    }

    public final class AreaSettingsDetail {

        @NonNull
        public final AreaSettings areaSettings;

        public Area area;

        public AreaDescription areaDescription;

        private AreaSettingsDetail(@NonNull AreaSettings areaSettings) {
            this.areaSettings = areaSettings;
        }
    }
}
