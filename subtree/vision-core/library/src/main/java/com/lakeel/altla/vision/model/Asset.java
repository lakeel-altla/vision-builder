package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import org.parceler.Transient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Asset extends BaseEntity {

    private final AssetType type;

    private String name;

    protected Asset(@NonNull AssetType type) {
        this.type = type;
    }

    @Exclude
    @Transient
    @NonNull
    public AssetType getType() {
        return type;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }
}
