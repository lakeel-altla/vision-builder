package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.ImageAsset;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import java.io.File;

final class ModelBuilder extends AssetBuilder {

    private SimpleArrayMap<String, AssetBuilder> modelBuilderMap = new SimpleArrayMap<>();

    ModelBuilder(@NonNull AssetBuilderContext context) {
        super(context);

        modelBuilderMap.put(ImageAsset.TYPE, new ImageAssetModelBuilder(context));
    }

    @Override
    void build(@NonNull String assetId, @NonNull String assetType, @NonNull File assetFile,
               @Nullable OnSuccessListener<Object> onSuccessListener, @Nullable OnFailureListener onFailureListener) {

        // On the loader thread.
        final AssetBuilder modelBuilder = modelBuilderMap.get(assetType);
        if (modelBuilder == null) {
            throw new IllegalArgumentException("The value of 'assetType' is not supported: assetType = " + assetType);
        }

        modelBuilder.build(assetId, assetType, assetFile, onSuccessListener, onFailureListener);
    }
}
