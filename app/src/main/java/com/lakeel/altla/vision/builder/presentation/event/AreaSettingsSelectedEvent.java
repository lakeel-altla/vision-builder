package com.lakeel.altla.vision.builder.presentation.event;

import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;

public final class AreaSettingsSelectedEvent {

    @NonNull
    public final AreaSettings areaSettings;

    @NonNull
    public final Area area;

    @NonNull
    public final AreaDescription areaDescription;

    public AreaSettingsSelectedEvent(@NonNull AreaSettings areaSettings, @NonNull Area area,
                                     @NonNull AreaDescription areaDescription) {
        this.areaSettings = areaSettings;
        this.area = area;
        this.areaDescription = areaDescription;
    }
}
