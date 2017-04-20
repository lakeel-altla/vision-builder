package com.lakeel.altla.vision.model;

import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;

import com.lakeel.altla.tango.TangoAreaDescriptionMetaDataHelper;

import org.parceler.Parcel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class TangoAreaDescription {

    private String areaDescriptionId;

    private String name;

    private long createdAt;

    @NonNull
    public static TangoAreaDescription toTangoAreaDescription(@NonNull TangoAreaDescriptionMetaData metaData) {
        TangoAreaDescription tangoAreaDescription = new TangoAreaDescription();
        tangoAreaDescription.setAreaDescriptionId(TangoAreaDescriptionMetaDataHelper.getUuid(metaData));
        tangoAreaDescription.setName(TangoAreaDescriptionMetaDataHelper.getName(metaData));
        tangoAreaDescription.setCreatedAt(TangoAreaDescriptionMetaDataHelper.getMsSinceEpoch(metaData));
        return tangoAreaDescription;
    }

    @Nullable
    public String getAreaDescriptionId() {
        return areaDescriptionId;
    }

    public void setAreaDescriptionId(@Nullable String areaDescriptionId) {
        this.areaDescriptionId = areaDescriptionId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
