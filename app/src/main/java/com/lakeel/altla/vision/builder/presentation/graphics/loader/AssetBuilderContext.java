package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

interface AssetBuilderContext {

    <T> void load(@NonNull Class<T> clazz, @NonNull String assetId, @NonNull String assetType,
                  @Nullable OnSuccessListener<T> onSuccessListener,
                  @Nullable OnFailureListener onFailureListener);

    void runOnLoaderThread(@NonNull Runnable runnable);
}
