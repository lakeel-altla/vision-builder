package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.AreaDescription;

import android.support.annotation.NonNull;

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
                             getLog().e(String.format("Failed to saveActor: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<AreaDescription> find(@NonNull String userId, @NonNull String areaDescriptionId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(areaDescriptionId);
        return new TypedQuery<>(reference, AreaDescription.class);
    }

    @NonNull
    public TypedQuery<AreaDescription> findAll(@NonNull String userId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .orderByChild(FIELD_NAME);
        return new TypedQuery<>(query, AreaDescription.class);
    }

    @NonNull
    public TypedQuery<AreaDescription> findByAreaId(@NonNull String userId, @NonNull String areaId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .orderByChild(FIELD_AREA_ID)
                                         .equalTo(areaId);
        return new TypedQuery<AreaDescription>(query, AreaDescription.class);
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
