package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;
import org.parceler.Transient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class MeshActor extends Actor {

    private String assetType = AssetType.UNKNOWN.name();

    private String assetId;

    @NonNull
    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(@NonNull String assetType) {
        this.assetType = assetType;
    }

    @Exclude
    @Transient
    @NonNull
    public AssetType getAssetTypeAsEnum() {
        return AssetType.valueOf(assetType);
    }

    public void setAssetTypeAsEnum(@NonNull AssetType assetType) {
        this.assetType = assetType.name();
    }

    @Nullable
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(@Nullable String assetId) {
        this.assetId = assetId;
    }

    @Exclude
    @Transient
    @NonNull
    public String getRequiredAssetId() {
        if (assetId == null) throw new IllegalStateException("'assetId' is null.");
        return assetId;
    }
}
