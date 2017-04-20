package com.lakeel.altla.vision.model;

import org.parceler.Parcel;

import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class ImageAssetFileUploadTask extends BaseEntity {

    private String instanceId;

    private String sourceUriString;

    @Nullable
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(@Nullable String instanceId) {
        this.instanceId = instanceId;
    }

    @Nullable
    public String getSourceUriString() {
        return sourceUriString;
    }

    public void setSourceUriString(@Nullable String sourceUriString) {
        this.sourceUriString = sourceUriString;
    }
}
