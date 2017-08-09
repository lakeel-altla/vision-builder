package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public interface AssetCacheLoader {

    void loadUserAssetFile(@NonNull String assetId, @NonNull String assetType,
                           @Nullable OnSuccessListener<File> onSuccessListener,
                           @Nullable OnFailureListener onFailureListener,
                           @Nullable OnProgressListener onProgressListener);
}
