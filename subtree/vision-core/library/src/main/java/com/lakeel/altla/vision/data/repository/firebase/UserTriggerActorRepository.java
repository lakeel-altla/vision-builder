package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.TriggerActor;

import android.support.annotation.NonNull;

public final class UserTriggerActorRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userTriggerActors";

    public UserTriggerActorRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull String areaId, @NonNull TriggerActor actor) {
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
    public TypedQuery<TriggerActor> find(@NonNull String userId, @NonNull String areaId, @NonNull String actorId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(areaId)
                                                         .child(actorId);
        return new TypedQuery<>(reference, TriggerActor.class);
    }

    @NonNull
    public TypedQuery<TriggerActor> findByAreaId(@NonNull String userId, @NonNull String areaId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .child(areaId);
        return new TypedQuery<>(query, TriggerActor.class);
    }

    public void delete(@NonNull String areaId, @NonNull TriggerActor actor) {
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
