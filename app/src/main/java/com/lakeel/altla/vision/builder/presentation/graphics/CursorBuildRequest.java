package com.lakeel.altla.vision.builder.presentation.graphics;

import com.lakeel.altla.vision.model.Asset;
import com.lakeel.altla.vision.model.AssetType;

import android.support.annotation.NonNull;

public final class CursorBuildRequest {

    public final Asset asset;

    public final AssetType assetType;

    public final AssetModelBuilder builder;

    public CursorBuildRequest(@NonNull Asset asset, @NonNull AssetType assetType, @NonNull AssetModelBuilder builder) {
        this.asset = asset;
        this.assetType = assetType;
        this.builder = builder;
    }
}
