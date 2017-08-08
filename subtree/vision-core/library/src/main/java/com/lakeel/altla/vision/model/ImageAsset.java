package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class ImageAsset extends Asset {

    public static final String TYPE = "Image";

    private boolean fileUploaded;

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }
}
