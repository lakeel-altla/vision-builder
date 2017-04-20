package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserActorRepository;
import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class UserActorApi extends BaseVisionApi {

    private final UserActorRepository userActorRepository;

    public UserActorApi(@NonNull VisionService visionService) {
        super(visionService);

        userActorRepository = new UserActorRepository(visionService.getFirebaseDatabase());
    }

    public void findActorById(@NonNull String actorId,
                              @Nullable OnSuccessListener<Actor> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener) {
        userActorRepository.find(CurrentUser.getInstance().getUserId(), actorId, onSuccessListener, onFailureListener);
    }

    public void findActorsByAreaId(@NonNull String areaId,
                                   @Nullable OnSuccessListener<List<Actor>> onSuccessListener,
                                   @Nullable OnFailureListener onFailureListener) {
        userActorRepository.findByAreaId(CurrentUser.getInstance().getUserId(), areaId,
                                         onSuccessListener, onFailureListener);
    }

    public ObservableData<Actor> observeActorById(@NonNull String actorId) {
        return userActorRepository.observe(CurrentUser.getInstance().getUserId(), actorId);
    }

    public void saveActor(@NonNull Actor actor) {
        if (!CurrentUser.getInstance().getUserId().equals(actor.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userActorRepository.save(actor);
    }

    public void deleteActorById(@NonNull String actorId) {
        userActorRepository.delete(CurrentUser.getInstance().getUserId(), actorId);
    }
}
