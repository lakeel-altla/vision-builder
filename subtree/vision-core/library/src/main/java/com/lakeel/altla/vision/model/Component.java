package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Component extends BaseEntity {

    public static final String FIELD_TYPE = "type";

    private String name;

    Component() {
    }

    @NonNull
    public abstract String getType();

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }
}
