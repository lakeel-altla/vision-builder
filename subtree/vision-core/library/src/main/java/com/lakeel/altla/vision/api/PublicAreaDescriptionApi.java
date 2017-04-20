package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.PublicAreaDescriptionRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.AreaDescription;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class PublicAreaDescriptionApi extends BaseVisionApi {

    private final PublicAreaDescriptionRepository publicAreaDescriptionRepository;

    public PublicAreaDescriptionApi(@NonNull VisionService visionService) {
        super(visionService);

        publicAreaDescriptionRepository = new PublicAreaDescriptionRepository(visionService.getFirebaseDatabase());
    }

    public void findAreaDescriptionById(@NonNull String areaDescriptionId,
                                        @Nullable OnSuccessListener<AreaDescription> onSuccessListener,
                                        @Nullable OnFailureListener onFailureListener) {
        publicAreaDescriptionRepository.find(areaDescriptionId,
                                             onSuccessListener, onFailureListener);
    }

    public void findAreaDescriptionsByAreaId(@NonNull String areaId,
                                             @Nullable OnSuccessListener<List<AreaDescription>> onSuccessListener,
                                             @Nullable OnFailureListener onFailureListener) {
        publicAreaDescriptionRepository.findByAreaId(areaId,
                                                     onSuccessListener, onFailureListener);
    }
}
