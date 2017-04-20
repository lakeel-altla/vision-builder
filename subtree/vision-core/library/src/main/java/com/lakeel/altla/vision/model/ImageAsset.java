package com.lakeel.altla.vision.model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public final class ImageAsset extends Asset {

    private boolean fileUploaded;

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }
}
