package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.ActorType;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class UserActorRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userActors";

    private static final String FIELD_TYPE = "type";

    public UserActorRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull String areaId, @NonNull Actor actor) {
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
    public TypedQuery<Actor> find(@NonNull String userId, @NonNull String areaId, @NonNull String actorId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(areaId)
                                                         .child(actorId);
        return new TypedQuery<>(reference, this::convert);
    }

    @NonNull
    public TypedQuery<Actor> findByAreaId(@NonNull String userId, @NonNull String areaId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .child(areaId);
        return new TypedQuery<>(query, this::convert);
    }

    public void delete(@NonNull String areaId, @NonNull Actor actor) {
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

    @Nullable
    private Actor convert(@NonNull DataSnapshot snapshot) {
        final String actorTypeString = (String) snapshot.child(FIELD_TYPE).getValue();
        if (actorTypeString == null) {
            return null;
        } else {
            final ActorType actorType = ActorType.valueOf(actorTypeString);
            return snapshot.getValue(actorType.actorClass);
        }
    }
}
