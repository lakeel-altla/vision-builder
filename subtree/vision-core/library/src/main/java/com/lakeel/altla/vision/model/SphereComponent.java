package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class SphereComponent extends ShapeComponent {

    public static final String TYPE = "Sphere";

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }
}
