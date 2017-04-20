package com.lakeel.altla.vision.model;

import org.parceler.Parcel;

import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class Profile extends BaseEntity {

    private String displayName;

    private String email;

    private String photoUri;

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @Nullable
    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(@Nullable String photoUri) {
        this.photoUri = photoUri;
    }
}
