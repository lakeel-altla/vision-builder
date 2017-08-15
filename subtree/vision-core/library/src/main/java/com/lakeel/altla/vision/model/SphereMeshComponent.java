package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class SphereMeshComponent extends PrimitiveMeshComponent {

    public static final String TYPE = "SphereMesh";

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }
}
