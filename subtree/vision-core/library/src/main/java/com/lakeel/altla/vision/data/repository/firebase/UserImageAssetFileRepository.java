package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.UploadTask;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

public final class UserImageAssetFileRepository extends BaseStorageRepository {

    private static final String BASE_PATH = "userImageAssets";

    public UserImageAssetFileRepository(@NonNull FirebaseStorage storage) {
        super(storage);
    }

    @NonNull
    public UploadTask upload(@NonNull String userId, @NonNull String assetId, @NonNull InputStream stream) {
        return getStorage().getReference()
                           .child(BASE_PATH)
                           .child(userId)
                           .child(assetId)
                           .putStream(stream);
    }

    @NonNull
    public FileDownloadTask download(@NonNull String userId, @NonNull String assetId, @NonNull File destination) {
        return getStorage().getReference()
                           .child(BASE_PATH)
                           .child(userId)
                           .child(assetId)
                           .getFile(destination);
    }

    public void delete(@NonNull String userId, @NonNull String assetId,
                       @Nullable OnSuccessListener<Void> onSuccessListener,
                       @Nullable OnFailureListener onFailureListener) {
        getStorage().getReference()
                    .child(BASE_PATH)
                    .child(userId)
                    .child(assetId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        if (onSuccessListener != null) onSuccessListener.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof StorageException) {
                            StorageException storageException = (StorageException) e;
                            if (storageException.getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                if (onSuccessListener != null) onSuccessListener.onSuccess(null);
                            }
                        } else {
                            if (onFailureListener != null) onFailureListener.onFailure(e);
                        }
                    });
    }
}
