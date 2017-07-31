package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.MeshActor;

import android.support.annotation.NonNull;

public final class UserMeshActorRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userMeshActors";

    public UserMeshActorRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull String areaId, @NonNull MeshActor actor) {
        actor.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(actor.getRequiredUserId())
                     .child(areaId)
                     .child(actor.getId())
                     .setValue(actor, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<MeshActor> find(@NonNull String userId, @NonNull String areaId, @NonNull String actorId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(areaId)
                                                         .child(actorId);
        return new TypedQuery<>(reference, MeshActor.class);
    }

    @NonNull
    public TypedQuery<MeshActor> findByAreaId(@NonNull String userId, @NonNull String areaId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .child(areaId);
        return new TypedQuery<>(query, MeshActor.class);
    }

    public void delete(@NonNull String areaId, @NonNull MeshActor actor) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(actor.getRequiredUserId())
                     .child(areaId)
                     .child(actor.getId())
                     .removeValue((error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to remove: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }
}
