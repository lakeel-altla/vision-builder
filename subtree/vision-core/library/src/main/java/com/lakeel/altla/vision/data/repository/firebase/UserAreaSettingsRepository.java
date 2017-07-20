package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;

public final class UserAreaSettingsRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userAreaSettings";

    private static final String FIELD_UPDATED_AT = "updatedAt";

    public UserAreaSettingsRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull AreaSettings areaSettings) {
        if (areaSettings.getUserId() == null) {
            throw new IllegalArgumentException("areaSettings.getUserId() must be not null.");
        }

        areaSettings.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(areaSettings.getUserId())
                     .child(areaSettings.getId())
                     .setValue(areaSettings, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to saveActor: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<AreaSettings> findAll(@NonNull String userId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId)
                                         .orderByChild(FIELD_UPDATED_AT);
        return new TypedQuery<>(query, AreaSettings.class);
    }
}
