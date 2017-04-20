package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.PublicAreaRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Area;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class PublicAreaApi extends BaseVisionApi {

    private final PublicAreaRepository publicAreaRepository;

    public PublicAreaApi(@NonNull VisionService visionService) {
        super(visionService);

        publicAreaRepository = new PublicAreaRepository(visionService.getFirebaseDatabase());
    }

    public void findAreaById(@NonNull String areaId,
                             @Nullable OnSuccessListener<Area> onSuccessListener,
                             @Nullable OnFailureListener onFailureListener) {
        publicAreaRepository.find(areaId, onSuccessListener, onFailureListener);
    }

    public void findAreasByPlaceId(@NonNull String placeId,
                                   @Nullable OnSuccessListener<List<Area>> onSuccessListener,
                                   @Nullable OnFailureListener onFailureListener) {
        publicAreaRepository.findByPlaceId(placeId, onSuccessListener, onFailureListener);
    }
}
