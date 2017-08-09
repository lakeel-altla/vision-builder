package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public final class AssetLoaderManager implements AssetLoaderContext {

    private final HandlerThread handlerThread = new HandlerThread("AssetLoader");

    private final Handler handler;

    private final VisionService visionService;

    private TextureLoader textureLoader;

    private ImageAssetModelLoader imageAssetModelLoader;

    public AssetLoaderManager(@NonNull VisionService visionService) {
        this.visionService = visionService;
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void loadAssetFile(@NonNull String assetId,
                              @Nullable OnSuccessListener<File> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener,
                              @Nullable OnProgressListener onProgressListener) {

        // TODO: how should we load public assets?
        visionService.getUserAssetApi()
                     .loadAssetFile(assetId, file -> {
                         if (onSuccessListener != null) {
                             runOnLoaderThread(() -> onSuccessListener.onSuccess(file));
                         }
                     }, e -> {
                         if (onFailureListener != null) {
                             runOnLoaderThread(() -> onFailureListener.onFailure(e));
                         }
                     }, (totalBytes, bytesTransferred) -> {
                         if (onProgressListener != null) {
                             runOnLoaderThread(() -> onProgressListener.onProgress(totalBytes, bytesTransferred));
                         }
                     });
    }

    @Override
    public void runOnLoaderThread(@NonNull Runnable runnable) {
        handler.post(runnable);
    }

    @NonNull
    public AssetLoader<Texture> getTextureLoader() {
        if (textureLoader == null) textureLoader = new TextureLoader(this);
        return textureLoader;
    }

    @NonNull
    public AssetLoader<Model> getImageAssetModelLoader() {
        if (imageAssetModelLoader == null) imageAssetModelLoader = new ImageAssetModelLoader(this, getTextureLoader());
        return imageAssetModelLoader;
    }
}
