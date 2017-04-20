package com.lakeel.altla.vision.model;

import org.parceler.Parcel;

import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class Area extends BaseEntity {

    private String name;

    private String placeId;

    private int level;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(@Nullable String placeId) {
        this.placeId = placeId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
