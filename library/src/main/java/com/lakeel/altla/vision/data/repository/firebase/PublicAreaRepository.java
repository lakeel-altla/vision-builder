package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.domain.helper.OnFailureListener;
import com.lakeel.altla.vision.domain.helper.OnSuccessListener;
import com.lakeel.altla.vision.domain.model.Area;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PublicAreaRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "publicAreas";

    private static final String FIELD_PLACE_ID = "placeId";

    public PublicAreaRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void findByPlaceId(@NonNull String placeId,
                              @Nullable OnSuccessListener<List<Area>> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .orderByChild(FIELD_PLACE_ID)
                     .equalTo(placeId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             List<Area> list = new ArrayList<>((int) snapshot.getChildrenCount());
                             for (DataSnapshot child : snapshot.getChildren()) {
                                 list.add(child.getValue(Area.class));
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
