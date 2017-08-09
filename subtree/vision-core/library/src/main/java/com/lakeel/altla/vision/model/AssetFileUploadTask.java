package com.lakeel.altla.vision.model;

import android.support.annotation.Nullable;

public final class AssetFileUploadTask extends BaseEntity {

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
