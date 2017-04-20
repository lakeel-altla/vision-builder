package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

public final class UserImageAssetFileRepository extends BaseStorageRepository {

    private static final String BASE_PATH = "userImageAssets";

    public UserImageAssetFileRepository(@NonNull FirebaseStorage storage) {
        super(storage);
    }

    public void exists(@NonNull String userId, @NonNull String assetId,
                       @Nullable OnSuccessListener<Boolean> onSuccessListener,
                       @Nullable OnFailureListener onFailureListener) {
        getStorage().getReference()
                    .child(BASE_PATH)
                    .child(userId)
                    .child(assetId)
                    .getMetadata()
                    .addOnSuccessListener(metadata -> {
                        if (onSuccessListener != null) onSuccessListener.onSuccess(Boolean.TRUE);
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof StorageException) {
                            StorageException storageException = (StorageException) e;
                            if (storageException.getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                if (onSuccessListener != null) onSuccessListener.onSuccess(Boolean.FALSE);
                            }
                        } else {
                            if (onFailureListener != null) onFailureListener.onFailure(e);
                        }
                    });
    }

    public void upload(@NonNull String userId, @NonNull String assetId,
                       @NonNull InputStream stream, OnSuccessListener<Void> onSuccessListener,
                       @Nullable OnFailureListener onFailureListener, @Nullable OnProgressListener onProgressListener) {
        getStorage().getReference()
                    .child(BASE_PATH)
                    .child(userId)
                    .child(assetId)
                    .putStream(stream)
                    .addOnSuccessListener(snapshot -> {
                        if (onSuccessListener != null) onSuccessListener.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    })
                    .addOnProgressListener(snapshot -> {
                        if (onProgressListener != null) {
                            onProgressListener.onProgress(snapshot.getTotalByteCount(),
                                                          snapshot.getBytesTransferred());
                        }
                    });
    }

    public void download(@NonNull String userId, @NonNull String assetId, @NonNull File destination,
                         @Nullable OnSuccessListener<Void> onSuccessListener,
                         @Nullable OnFailureListener onFailureListener,
                         @Nullable OnProgressListener onProgressListener) {
        getStorage().getReference()
                    .child(BASE_PATH)
                    .child(userId)
                    .child(assetId)
                    .getFile(destination)
                    .addOnSuccessListener(snapshot -> {
                        if (onSuccessListener != null) onSuccessListener.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    })
                    .addOnProgressListener(snapshot -> {
                        if (onProgressListener != null) {
                            onProgressListener.onProgress(snapshot.getTotalByteCount(),
                                                          snapshot.getBytesTransferred());
                        }
                    });
    }

    public void getDownloadUri(@NonNull String userId, @NonNull String assetId,
                               @Nullable OnSuccessListener<Uri> onSuccessListener,
                               @Nullable OnFailureListener onFailureListener) {
        getStorage().getReference()
                    .child(BASE_PATH)
                    .child(userId)
                    .child(assetId)
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        if (onSuccessListener != null) onSuccessListener.onSuccess(uri);
                    })
                    .addOnFailureListener(e -> {
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    });
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
