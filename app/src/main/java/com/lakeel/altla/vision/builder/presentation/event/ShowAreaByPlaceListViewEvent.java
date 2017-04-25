package com.lakeel.altla.vision.builder.presentation.event;

import com.google.android.gms.location.places.Place;

import android.support.annotation.NonNull;

public final class ShowAreaByPlaceListViewEvent {

    @NonNull
    public final Place place;

    public ShowAreaByPlaceListViewEvent(@NonNull Place place) {
        this.place = place;
    }
}
