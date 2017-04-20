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
import com.lakeel.altla.vision.model.Area;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class UserAreaRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userAreas";

    private static final String FIELD_NAME = "name";

    private static final String FIELD_PLACE_ID = "placeId";

    public UserAreaRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull Area area) {
        if (area.getUserId() == null) throw new IllegalArgumentException("area.getUserId() must be not null.");

        area.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(area.getUserId())
                     .child(area.getId())
                     .setValue(area, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    public void find(@NonNull String userId, @NonNull String areaId,
                     @Nullable OnSuccessListener<Area> onSuccessListener,
                     @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(areaId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             Area area = snapshot.getValue(Area.class);
                             if (onSuccessListener != null) onSuccessListener.onSuccess(area);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             if (onFailureListener != null) onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    public void findAll(@NonNull String userId,
                        @Nullable OnSuccessListener<List<Area>> onSuccessListener,
                        @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .orderByChild(FIELD_NAME)
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

    public void findByPlaceId(@NonNull String userId, @NonNull String placeId,
                              @Nullable OnSuccessListener<List<Area>> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
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

    @NonNull
    public ObservableData<Area> observe(@NonNull String userId, @NonNull String areaId) {
        DatabaseReference reference = getDatabase().getReference()
                                                   .child(BASE_PATH)
                                                   .child(userId)
                                                   .child(areaId);

        return new FirebaseObservableData<>(reference, snapshot -> snapshot.getValue(Area.class));
    }

    @NonNull
    public ObservableList<Area> observeAll(@NonNull String userId) {
        Query query = getDatabase().getReference()
                                   .child(BASE_PATH)
                                   .child(userId)
                                   .orderByChild(FIELD_NAME);

        return new FirebaseObservableList<>(query, snapshot -> snapshot.getValue(Area.class));
    }

    public void delete(@NonNull String userId, @NonNull String areaId) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(areaId)
                     .removeValue((error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to remove: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }
}
