package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class UserActorRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userActors";

    private static final String FIELD_AREA_ID = "areaId";

    public UserActorRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull Actor actor) {
        if (actor.getUserId() == null) throw new IllegalArgumentException("actor.getUserId() must be not null.");
        if (actor.getAreaId() == null) throw new IllegalArgumentException("actor.getSceneId() must be not null.");

        actor.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(actor.getUserId())
                     .child(actor.getId())
                     .setValue(actor, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to saveActor: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<Actor> find(@NonNull String userId, @NonNull String actorId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(actorId);
        return new TypedQuery<>(reference, Actor.class);
    }

    @NonNull
    public TypedQuery<Actor> findByAreaId(@NonNull String userId, @NonNull String areaId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .orderByChild(FIELD_AREA_ID)
                                         .equalTo(areaId);
        return new TypedQuery<>(query, Actor.class);
    }

    public void delete(@NonNull String userId, @NonNull String actorId) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(actorId)
                     .removeValue((error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to remove: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }
}
