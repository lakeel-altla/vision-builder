package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.PublicActorRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class PublicActorApi extends BaseVisionApi {

    private final PublicActorRepository publicActorRepository;

    public PublicActorApi(@NonNull VisionService visionService) {
        super(visionService);

        publicActorRepository = new PublicActorRepository(visionService.getFirebaseDatabase());
    }

    public void findActorById(@NonNull String actorId,
                              @Nullable OnSuccessListener<Actor> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener) {
        publicActorRepository.find(actorId, onSuccessListener, onFailureListener);
    }

    public void findActorsByAreaId(@NonNull String areaId,
                                   @Nullable OnSuccessListener<List<Actor>> onSuccessListener,
                                   @Nullable OnFailureListener onFailureListener) {
        publicActorRepository.findByAreaId(areaId, onSuccessListener, onFailureListener);
    }
}
