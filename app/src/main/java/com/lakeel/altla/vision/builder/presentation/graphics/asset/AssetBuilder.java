package com.lakeel.altla.vision.builder.presentation.graphics.asset;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

abstract class AssetBuilder {

    protected AssetBuilderContext context;

    private boolean initialized;

    boolean isInitialized() {
        return initialized;
    }

    void initialize(@NonNull AssetBuilderContext context) {
        this.context = context;
        initialized = true;
    }

    abstract Class<?> getTargetType();

    abstract void build(@NonNull String assetId, @NonNull String assetType, @NonNull File assetFile,
                        @Nullable OnSuccessListener<Object> onSuccessListener,
                        @Nullable OnFailureListener onFailureListener);
}
