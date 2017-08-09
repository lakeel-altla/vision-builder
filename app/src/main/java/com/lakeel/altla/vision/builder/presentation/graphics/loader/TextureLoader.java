package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

final class TextureLoader extends AssetLoader<Texture> {

    TextureLoader(@NonNull AssetLoaderContext context) {
        super(context);
    }

    @Override
    protected void loadCore(@NonNull String assetId,
                            @Nullable OnSuccessListener<Texture> onSuccessListener,
                            @Nullable OnFailureListener onFailureListener,
                            @Nullable OnProgressListener onProgressListener) {
        context.loadAssetFile(assetId, file -> {
            // On the loader thread.
            try {
                final FileHandle fileHandle = Gdx.files.absolute(file.getPath());

                // Load contents on the loader thread.
                final TextureData data = TextureData.Factory.loadFromFile(fileHandle, null, false);

                // Create a texture on the graphics thread.
                Gdx.app.postRunnable(() -> {
                    try {
                        if (onSuccessListener != null) onSuccessListener.onSuccess(new Texture(data));
                    } catch (RuntimeException e) {
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    }
                });
            } catch (RuntimeException e) {
                if (onFailureListener != null) onFailureListener.onFailure(e);
            }

        }, e -> {
            if (onFailureListener != null) onFailureListener.onFailure(e);
        }, (totalBytes, bytesTransferred) -> {
            if (onProgressListener != null) onProgressListener.onProgress(totalBytes, bytesTransferred);
        });
    }
}
