package com.lakeel.altla.vision.builder.presentation.graphics;

import com.lakeel.altla.vision.model.AssetType;

import android.support.annotation.NonNull;

public final class CursorBuildRequest {

    public final String assetId;

    public final AssetType assetType;

    public final AssetModelBuilder builder;

    public CursorBuildRequest(@NonNull String assetId, @NonNull AssetType assetType,
                              @NonNull AssetModelBuilder builder) {
        this.assetId = assetId;
        this.assetType = assetType;
        this.builder = builder;
    }
}
