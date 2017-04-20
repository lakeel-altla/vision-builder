package com.lakeel.altla.vision.model;

import org.parceler.Parcel;

import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class Device extends BaseEntity {

    private String osName;

    private String osModel;

    private String osVersion;

    @Nullable
    public String getOsName() {
        return osName;
    }

    public void setOsName(@Nullable String osName) {
        this.osName = osName;
    }

    @Nullable
    public String getOsModel() {
        return osModel;
    }

    public void setOsModel(@Nullable String osModel) {
        this.osModel = osModel;
    }

    @Nullable
    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(@Nullable String osVersion) {
        this.osVersion = osVersion;
    }
}
