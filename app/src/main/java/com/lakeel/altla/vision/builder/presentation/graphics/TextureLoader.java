package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class TextureLoader {

    private final HandlerThread handlerThread = new HandlerThread("TextureLoader");

    private final Handler handler;

    public TextureLoader() {
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void load(@NonNull FileHandle fileHandle,
                     @Nullable OnSuccessListener<Texture> onSuccessListener,
                     @Nullable OnFailureListener onFailureListener) {
        handler.post(() -> {
            try {
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
        });
    }
}
