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
import com.lakeel.altla.vision.model.AreaDescription;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class UserAreaDescriptionRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userAreaDescriptions";

    private static final String FIELD_NAME = "name";

    private static final String FIELD_AREA_ID = "areaId";

    public UserAreaDescriptionRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull AreaDescription areaDescription) {
        if (areaDescription.getUserId() == null) {
            throw new IllegalArgumentException("areaDescription.getUserId() must be not null.");
        }

        areaDescription.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(areaDescription.getUserId())
                     .child(areaDescription.getId())
                     .setValue(areaDescription, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    public void find(@NonNull String userId, @NonNull String areaDescriptionId,
                     OnSuccessListener<AreaDescription> onSuccessListener, OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
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

    public void findAll(@NonNull String userId, OnSuccessListener<List<AreaDescription>> onSuccessListener,
                        OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .orderByChild(FIELD_NAME)
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

    public void findByAreaId(@NonNull String userId, @NonNull String areaId,
                             OnSuccessListener<List<AreaDescription>> onSuccessListener,
                             OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
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

    @NonNull
    public ObservableData<AreaDescription> observe(@NonNull String userId, @NonNull String areaDescriptionId) {
        DatabaseReference reference = getDatabase().getReference()
                                                   .child(BASE_PATH)
                                                   .child(userId)
                                                   .child(areaDescriptionId);

        return new FirebaseObservableData<>(reference, snapshot -> snapshot.getValue(AreaDescription.class));
    }

    @NonNull
    public ObservableList<AreaDescription> observeAll(@NonNull String userId) {
        Query query = getDatabase().getReference()
                                   .child(BASE_PATH)
                                   .child(userId)
                                   .orderByChild(FIELD_NAME);

        return new FirebaseObservableList<>(query, snapshot -> snapshot.getValue(AreaDescription.class));
    }

    @NonNull
    public ObservableList<AreaDescription> observeByAreaId(@NonNull String userId, @NonNull String areaId) {
        Query query = getDatabase().getReference()
                                   .child(BASE_PATH)
                                   .child(userId)
                                   .orderByChild(FIELD_AREA_ID)
                                   .equalTo(areaId);

        return new FirebaseObservableList<>(query, snapshot -> snapshot.getValue(AreaDescription.class));
    }

    public void delete(@NonNull String userId, @NonNull String areaDescriptionId) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(areaDescriptionId)
                     .removeValue((error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to remove: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }
}
