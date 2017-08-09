package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.AssetFileUploadTask;

import android.support.annotation.NonNull;

public final class UserAssetFileUploadTaskRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userAssetFileUploadTasks";

    public UserAssetFileUploadTaskRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull AssetFileUploadTask task) {
        if (task.getUserId() == null) throw new IllegalArgumentException("task.getUserId() must be not null.");

        task.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(task.getUserId())
                     .child(task.getId())
                     .setValue(task, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to saveActor: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public TypedQuery<AssetFileUploadTask> findAll(@NonNull String userId) {
        final Query query = getDatabase().getReference()
                                         .child(BASE_PATH)
                                         .child(userId);
        return new TypedQuery<>(query, AssetFileUploadTask.class);
    }

    public void delete(@NonNull String userId, @NonNull String assetId) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(assetId)
                     .removeValue((error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to remove: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }
}
