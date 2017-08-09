package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public interface AssetLoaderContext {

    void loadAssetFile(@NonNull String assetId,
                       @Nullable OnSuccessListener<File> onSuccessListener,
                       @Nullable OnFailureListener onFailureListener,
                       @Nullable OnProgressListener onProgressListener);

    void runOnLoaderThread(@NonNull Runnable runnable);
}
