package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class AssetMeshComponent extends MeshComponent {

    public static final String TYPE = "AssetMesh";

    private String assetType;

    private String assetId;

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }

    @Nullable
    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(@Nullable String assetType) {
        this.assetType = assetType;
    }

    @Nullable
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(@Nullable String assetId) {
        this.assetId = assetId;
    }

    @Exclude
    @NonNull
    public String getRequiredAssetType() {
        if (assetType == null) throw new IllegalStateException("'assetType' is null.");
        return assetType;
    }

    @Exclude
    @NonNull
    public String getRequiredAssetId() {
        if (assetId == null) throw new IllegalStateException("'assetId' is null.");
        return assetId;
    }
}
