package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PublicActorRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "publicActors";

    private static final String FIELD_AREA_ID = "areaId";

    public PublicActorRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void find(@NonNull String actorId,
                     @Nullable OnSuccessListener<Actor> onSuccessListener,
                     @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
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

    public void findByAreaId(@NonNull String areaId,
                             @Nullable OnSuccessListener<List<Actor>> onSuccessListener,
                             @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
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
}
