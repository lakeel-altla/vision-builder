package com.lakeel.altla.vision.model;

import org.parceler.Parcel;

import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class AreaDescription extends BaseEntity {

    private String name;

    private boolean fileUploaded;

    private String areaId;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }

    @Nullable
    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(@Nullable String areaId) {
        this.areaId = areaId;
    }
}
