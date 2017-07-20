package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserActorRepository;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class UserActorApi extends BaseVisionApi {

    private final UserActorRepository userActorRepository;

    public UserActorApi(@NonNull VisionService visionService) {
        super(visionService);

        userActorRepository = new UserActorRepository(visionService.getFirebaseDatabase());
    }

    @NonNull
    public TypedQuery<Actor> findActor(@NonNull String actorId) {
        return userActorRepository.find(CurrentUser.getInstance().getUserId(), actorId);
    }

    @NonNull
    public TypedQuery<Actor> findActorByAreaId(@NonNull String areaId) {
        return userActorRepository.findByAreaId(CurrentUser.getInstance().getUserId(), areaId);
    }

    public void saveActor(@NonNull Actor actor) {
        if (!CurrentUser.getInstance().getUserId().equals(actor.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userActorRepository.save(actor);
    }

    public void deleteActor(@NonNull String actorId) {
        userActorRepository.delete(CurrentUser.getInstance().getUserId(), actorId);
    }
}
