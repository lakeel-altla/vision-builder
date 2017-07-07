package com.lakeel.altla.vision.builder.presentation.graphics;

import android.graphics.Bitmap;

public final class ImageActorBitmap {

    public final String actorId;

    public final Bitmap bitmap;

    public ImageActorBitmap(String actorId, Bitmap bitmap) {
        this.actorId = actorId;
        this.bitmap = bitmap;
    }
}
