package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public abstract class Component extends BaseEntity {

    public static final String FIELD_TYPE = "type";

    Component() {
    }

    @NonNull
    public abstract String getType();
}
