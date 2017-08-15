package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import android.support.annotation.NonNull;

public abstract class Component extends BaseEntity {

    public static final String FIELD_TYPE = "type";

    Component() {
    }

    @Exclude
    @NonNull
    public abstract String getType();
}
