package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.helper.FirebaseObservableData;
import com.lakeel.altla.vision.helper.FirebaseObservableList;
import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.ObservableList;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    public void find(@NonNull String userId, @NonNull String actorId,
                     @Nullable OnSuccessListener<Actor> onSuccessListener,
                     @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(actorId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             Actor actor = snapshot.getValue(Actor.class);
                             if (onSuccessListener != null) onSuccessListener.onSuccess(actor);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             if (onFailureListener != null) onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    public void findByAreaId(@NonNull String userId, @NonNull String areaId,
                             @Nullable OnSuccessListener<List<Actor>> onSuccessListener,
                             @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .orderByChild(FIELD_AREA_ID)
                     .equalTo(areaId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             List<Actor> list = new ArrayList<>((int) snapshot.getChildrenCount());
                             for (DataSnapshot child : snapshot.getChildren()) {
                                 list.add(child.getValue(Actor.class));
                             }
                             if (onSuccessListener != null) onSuccessListener.onSuccess(list);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             if (onFailureListener != null) onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    @NonNull
    public ObservableData<Actor> observe(@NonNull String userId, @NonNull String actorId) {
        DatabaseReference reference = getDatabase().getReference()
                                                   .child(BASE_PATH)
                                                   .child(userId)
                                                   .child(actorId);

        return new FirebaseObservableData<>(reference, snapshot -> snapshot.getValue(Actor.class));
    }

    @NonNull
    public ObservableList<Actor> observeAll(@NonNull String userId) {
        Query query = getDatabase().getReference()
                                   .child(BASE_PATH)
                                   .child(userId);

        return new FirebaseObservableList<>(query, snapshot -> snapshot.getValue(Actor.class));
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
