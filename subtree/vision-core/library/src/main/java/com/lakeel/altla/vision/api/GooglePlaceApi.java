package com.lakeel.altla.vision.api;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;

import com.lakeel.altla.vision.data.repository.android.PlaceRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class GooglePlaceApi extends BaseVisionApi {

    private final PlaceRepository placeRepository;

    GooglePlaceApi(@NonNull VisionService visionService) {
        super(visionService);

        placeRepository = new PlaceRepository();
    }

    public void getPlaceById(@NonNull GoogleApiClient googleApiClient, @NonNull String placeId,
                             @Nullable OnSuccessListener<Place> onSuccessListener,
                             @Nullable OnFailureListener onFailureListener) {
        placeRepository.get(googleApiClient, placeId, onSuccessListener, onFailureListener);
    }
}
