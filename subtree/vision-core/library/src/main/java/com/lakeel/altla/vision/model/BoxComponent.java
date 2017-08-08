package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class BoxComponent extends ShapeComponent {

    public static final String TYPE = "Box";

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }
}
