package com.lakeel.altla.vision.data.repository.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.lakeel.altla.rx.firebase.storage.RxFirebaseStorageTask;
import com.lakeel.altla.rx.tasks.RxGmsTask;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.domain.repository.UserAreaDescriptionFileRepository;

import java.io.File;
import java.io.InputStream;

import rx.Single;

public final class UserAreaDescriptionFileRepositoryImpl implements UserAreaDescriptionFileRepository {

    private static final String PATH_USER_AREA_DESCRIPTIONS = "userAreaDescriptions";

    private final StorageReference rootReference;

    public UserAreaDescriptionFileRepositoryImpl(StorageReference rootReference) {
        if (rootReference == null) throw new ArgumentNullException("rootReference");

        this.rootReference = rootReference;
    }

    @Override
    public Single<String> upload(String areaDescriptionId, InputStream stream, OnProgressListener onProgressListener) {
        if (areaDescriptionId == null) throw new ArgumentNullException("areaDescriptionId");
        if (stream == null) throw new ArgumentNullException("stream");

        // NOTE:
        //
        // Callbacks of the task for Firebase Storage are called by the thread for Firebase Storage.
        // Calling RxJava methods from them will be also called by its thread.
        // Note that a subsequent stream processing is also handled by its thread.

        StorageReference reference = rootReference.child(PATH_USER_AREA_DESCRIPTIONS)
                                                  .child(resolveCurrentUserId())
                                                  .child(areaDescriptionId);
        UploadTask task = reference.putStream(stream);

        return RxFirebaseStorageTask.asSingle(task, onProgressListener::onProgress)
                                    .map(snapshot -> areaDescriptionId);
    }

    @Override
    public Single<String> download(String areaDescriptionId, File destination, OnProgressListener onProgressListener) {
        if (areaDescriptionId == null) throw new ArgumentNullException("areaDescriptionId");
        if (destination == null) throw new ArgumentNullException("destination");

        StorageReference reference = rootReference.child(PATH_USER_AREA_DESCRIPTIONS)
                                                  .child(resolveCurrentUserId())
                                                  .child(areaDescriptionId);
        FileDownloadTask task = reference.getFile(destination);

        return RxFirebaseStorageTask.asSingle(task, onProgressListener::onProgress)
                                    .map(snapshot -> areaDescriptionId);
    }

    @Override
    public Single<String> delete(String areaDescriptionId) {
        if (areaDescriptionId == null) throw new ArgumentNullException("areaDescriptionId");

        StorageReference reference = rootReference.child(PATH_USER_AREA_DESCRIPTIONS)
                                                  .child(resolveCurrentUserId())
                                                  .child(areaDescriptionId);
        Task<Void> task = reference.delete();

        return RxGmsTask.asObservable(task).map(aVoid -> areaDescriptionId).toSingle();
    }

    private String resolveCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("The current user could not be resolved.");
        }
        return user.getUid();
    }
}
