package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserMeshActorRepository;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.MeshActor;

import android.support.annotation.NonNull;

public final class UserActorApi extends BaseVisionApi {

    private final UserMeshActorRepository userMeshActorRepository;

    public UserActorApi(@NonNull VisionService visionService) {
        super(visionService);

        userMeshActorRepository = new UserMeshActorRepository(visionService.getFirebaseDatabase());
    }

    @NonNull
    public TypedQuery<MeshActor> findMeshActor(@NonNull String areaId, @NonNull String actorId) {
        return userMeshActorRepository.find(CurrentUser.getInstance().getUserId(), areaId, actorId);
    }

    @NonNull
    public TypedQuery<MeshActor> findMeshActorsByAreaId(@NonNull String areaId) {
        return userMeshActorRepository.findByAreaId(CurrentUser.getInstance().getUserId(), areaId);
    }

    public void saveActor(@NonNull String areaId, @NonNull Actor actor) {
        getVisionService().throwsIfUserIdInvalid(actor);

        if (actor instanceof MeshActor) {
            userMeshActorRepository.save(areaId, (MeshActor) actor);
        } else {
            throw new IllegalArgumentException("A type of 'actor' is not supported.");
        }
    }

    public <T extends Actor> void deleteActor(@NonNull String areaId, @NonNull Actor actor) {
        getVisionService().throwsIfUserIdInvalid(actor);

        if (actor instanceof MeshActor) {
            userMeshActorRepository.delete(areaId, (MeshActor) actor);
        } else {
            throw new IllegalArgumentException("A type of 'actor' is not supported.");
        }
    }
}
