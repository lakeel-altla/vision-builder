package com.lakeel.altla.vision.data.repository.android;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class PlaceRepository {

    public void get(@NonNull GoogleApiClient googleApiClient, @NonNull String placeId,
                    @Nullable OnSuccessListener<Place> onSuccessListener,
                    @Nullable OnFailureListener onFailureListener) {
        Places.GeoDataApi.getPlaceById(googleApiClient, placeId)
                         .setResultCallback(places -> {
                             if (places.getStatus().isSuccess()) {
                                 Place place = places.get(0);

                                 if (onSuccessListener != null) onSuccessListener.onSuccess(place);
                             } else if (places.getStatus().isCanceled()) {
                                 if (onFailureListener != null) {
                                     onFailureListener.onFailure(new StatusException("Cancelled.", placeId));
                                 }
                             } else if (places.getStatus().isInterrupted()) {
                                 if (onFailureListener != null) {
                                     onFailureListener.onFailure(new StatusException("Interrupted.", placeId));
                                 }
                             } else {
                                 if (onFailureListener != null) {
                                     onFailureListener.onFailure(new StatusException("Unexpected Error.", placeId));
                                 }
                             }
                         });
    }

    public final class StatusException extends RuntimeException {

        private final String placeId;

        private StatusException(@NonNull String message, @NonNull String placeId) {
            super(message);
            this.placeId = placeId;
        }

        public String getPlaceId() {
            return placeId;
        }
    }
}
