package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

final class TextureBuilder extends AssetBuilder {

    TextureBuilder(@NonNull AssetBuilderContext context) {
        super(context);
    }

    @Override
    void build(@NonNull String assetId, @NonNull String assetType, @NonNull File assetFile,
               @Nullable OnSuccessListener<Object> onSuccessListener,
               @Nullable OnFailureListener onFailureListener) {

        // This method will be invoked on the loader thread.
        try {
            final FileHandle fileHandle = Gdx.files.absolute(assetFile.getPath());

            // Load contents on the loader thread.
            final TextureData data = TextureData.Factory.loadFromFile(fileHandle, null, false);

            // Create a texture on the graphics thread.
            Gdx.app.postRunnable(() -> {
                try {
                    final Texture texture = new Texture(data);

                    // Callback on the loadet thread.
                    if (onSuccessListener != null) {
                        context.runOnLoaderThread(() -> onSuccessListener.onSuccess(texture));
                    }
                } catch (RuntimeException e) {
                    // Callback on the loadet thread.
                    if (onFailureListener != null) {
                        context.runOnLoaderThread(() -> onFailureListener.onFailure(e));
                    }
                }
            });
        } catch (RuntimeException e) {
            if (onFailureListener != null) {
                onFailureListener.onFailure(e);
            }
        }
    }
}
