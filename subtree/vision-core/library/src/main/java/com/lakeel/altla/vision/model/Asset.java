package com.lakeel.altla.vision.model;

import android.support.annotation.Nullable;

public class Asset extends BaseEntity {

    private String name;

    protected Asset() {
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }
}
