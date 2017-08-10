package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

abstract class AssetBuilder {

    protected final AssetBuilderContext context;

    AssetBuilder(@NonNull AssetBuilderContext context) {
        this.context = context;
    }

    abstract void build(@NonNull String assetId, @NonNull String assetType, @NonNull File assetFile,
                        @Nullable OnSuccessListener<Object> onSuccessListener,
                        @Nullable OnFailureListener onFailureListener);
}
