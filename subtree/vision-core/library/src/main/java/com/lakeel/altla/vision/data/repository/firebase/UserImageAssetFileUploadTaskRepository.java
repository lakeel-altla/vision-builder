package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.lakeel.altla.vision.helper.FirebaseObservableList;
import com.lakeel.altla.vision.helper.ObservableList;
import com.lakeel.altla.vision.model.ImageAssetFileUploadTask;

import android.support.annotation.NonNull;

public final class UserImageAssetFileUploadTaskRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userImageAssetFileUploadTasks";

    private static final String FIELD_ORDER = "order";

    public UserImageAssetFileUploadTaskRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull ImageAssetFileUploadTask task) {
        if (task.getUserId() == null) throw new IllegalArgumentException("task.getUserId() must be not null.");

        task.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(task.getUserId())
                     .child(task.getId())
                     .setValue(task, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    @NonNull
    public ObservableList<ImageAssetFileUploadTask> observeAll(@NonNull String userId) {
        Query query = getDatabase().getReference()
                                   .child(BASE_PATH)
                                   .child(userId)
                                   .orderByChild(FIELD_ORDER);

        return new FirebaseObservableList<>(query, snapshot -> snapshot.getValue(ImageAssetFileUploadTask.class));
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
