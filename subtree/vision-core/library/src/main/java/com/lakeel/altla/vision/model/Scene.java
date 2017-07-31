package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;
import org.parceler.Transient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class Scene extends BaseEntity {

    private String areaId;

    private String scope = Scope.UNKNOWN.name();

    private String layer = Layer.UNKNOWN.name();

    @Nullable
    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(@Nullable String areaId) {
        this.areaId = areaId;
    }

    @NonNull
    public String getScope() {
        return scope;
    }

    public void setScope(@NonNull String scope) {
        this.scope = scope;
    }

    @Exclude
    @Transient
    @NonNull
    public Scope getScopeAsEnum() {
        return Scope.valueOf(scope);
    }

    public void setScopeAsEnum(@NonNull Scope scope) {
        this.scope = scope.name();
    }

    @NonNull
    public String getLayer() {
        return layer;
    }

    public void setLayer(@NonNull String layer) {
        this.layer = layer;
    }

    @Exclude
    @Transient
    @NonNull
    public Layer getLayerAsEnum() {
        return Layer.valueOf(layer);
    }

    public void setLayerAsEnum(@NonNull Layer layer) {
        this.layer = layer.name();
    }
}
