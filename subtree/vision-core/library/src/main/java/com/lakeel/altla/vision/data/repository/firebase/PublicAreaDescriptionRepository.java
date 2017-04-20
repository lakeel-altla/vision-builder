package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.AreaDescription;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class PublicAreaDescriptionRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "publicAreaDescriptions";

    private static final String FIELD_AREA_ID = "areaId";

    public PublicAreaDescriptionRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void find(@NonNull String areaDescriptionId,
                     OnSuccessListener<AreaDescription> onSuccessListener, OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(areaDescriptionId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             AreaDescription areaDescription = snapshot.getValue(AreaDescription.class);
                             if (onSuccessListener != null) onSuccessListener.onSuccess(areaDescription);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             if (onFailureListener != null) onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    public void findByAreaId(@NonNull String areaId,
                             OnSuccessListener<List<AreaDescription>> onSuccessListener,
                             OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .orderByChild(FIELD_AREA_ID)
                     .equalTo(areaId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             List<AreaDescription> list = new ArrayList<>((int) snapshot.getChildrenCount());
                             for (DataSnapshot child : snapshot.getChildren()) {
                                 list.add(child.getValue(AreaDescription.class));
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
