package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Objects;

public final class SelectAreaSettingsModel {

    private final VisionService visionService;

    private final ArModel arModel;

    @NonNull
    private Scope areaScope = Scope.USER;

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
}
