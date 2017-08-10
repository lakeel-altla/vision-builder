package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

interface AssetBuilderContext {

    void load(@NonNull Class<?> clazz, @NonNull String assetId, @NonNull String assetType,
              @Nullable OnSuccessListener<Object> onSuccessListener,
              @Nullable OnFailureListener onFailureListener);
}
