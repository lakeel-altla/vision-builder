package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    public void find(@NonNull String userId, @NonNull String areaSettingsId,
                     @Nullable OnSuccessListener<AreaSettings> onSuccessListener,
                     @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(areaSettingsId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             AreaSettings areaSettings = snapshot.getValue(AreaSettings.class);
                             if (onSuccessListener != null) onSuccessListener.onSuccess(areaSettings);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             if (onFailureListener != null) onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    public void findAll(@NonNull String userId,
                        @Nullable OnSuccessListener<List<AreaSettings>> onSuccessListener,
                        @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .orderByChild(FIELD_UPDATED_AT)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             List<AreaSettings> list = new ArrayList<>((int) snapshot.getChildrenCount());
                             for (DataSnapshot child : snapshot.getChildren()) {
                                 list.add(child.getValue(AreaSettings.class));
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
