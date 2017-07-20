package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Area;

import android.support.annotation.NonNull;

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
                             getLog().e(String.format("Failed to saveActor: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<Area> find(@NonNull String userId, @NonNull String areaId) {
        final DatabaseReference reference = getDatabase().getReference()
                                                         .child(BASE_PATH)
                                                         .child(userId)
                                                         .child(areaId);
        return new TypedQuery<>(reference, Area.class);
    }

    @NonNull
    public TypedQuery<Area> findAll(@NonNull String userId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .orderByChild(FIELD_NAME);
        return new TypedQuery<>(query, Area.class);
    }

    @NonNull
    public TypedQuery<Area> findByPlaceId(@NonNull String userId, @NonNull String placeId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .orderByChild(FIELD_PLACE_ID)
                                         .equalTo(placeId);
        return new TypedQuery<>(query, Area.class);
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
