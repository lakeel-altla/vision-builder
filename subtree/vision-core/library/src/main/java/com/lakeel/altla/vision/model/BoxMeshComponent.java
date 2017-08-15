package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class BoxMeshComponent extends PrimitiveMeshComponent {

    public static final String TYPE = "BoxMesh";

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }
}
