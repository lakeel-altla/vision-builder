package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;
import org.parceler.Transient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class AreaSettings extends BaseEntity {

    private String areaScope = Scope.UNKNOWN.name();

    private String areaId;

    private String areaDescriptionId;

    @NonNull
    public String getAreaScope() {
        return areaScope;
    }

    public void setAreaScope(@NonNull String areaScope) {
        this.areaScope = areaScope;
    }

    @Exclude
    @Transient
    @NonNull
    public Scope getAreaScopeAsEnum() {
        return Scope.valueOf(areaScope);
    }

    public void setAreaScopeAsEnum(@NonNull Scope scope) {
        this.areaScope = scope.name();
    }

    @Nullable
    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(@Nullable String areaId) {
        this.areaId = areaId;
    }

    @Nullable
    public String getAreaDescriptionId() {
        return areaDescriptionId;
    }

    public void setAreaDescriptionId(@Nullable String areaDescriptionId) {
        this.areaDescriptionId = areaDescriptionId;
    }
}
