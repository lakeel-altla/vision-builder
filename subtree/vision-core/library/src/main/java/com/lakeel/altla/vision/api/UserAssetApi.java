package com.lakeel.altla.vision.api;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;

import com.lakeel.altla.vision.data.repository.android.AssetCacheRepository;
import com.lakeel.altla.vision.data.repository.android.DocumentRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserImageAssetFileRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserImageAssetFileUploadTaskRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserImageAssetRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.model.ImageAssetFileUploadTask;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public final class UserAssetApi extends BaseVisionApi {

    private final UserImageAssetRepository userImageAssetRepository;

    private final UserImageAssetFileRepository userImageAssetFileRepository;

    private final UserImageAssetFileUploadTaskRepository userImageAssetFileUploadTaskRepository;

    private final DocumentRepository documentRepository;

    private final AssetCacheRepository assetCacheRepository;

    public UserAssetApi(@NonNull VisionService visionService) {
        super(visionService);

        userImageAssetRepository = new UserImageAssetRepository(visionService.getFirebaseDatabase());
        userImageAssetFileRepository = new UserImageAssetFileRepository(visionService.getFirebaseStorage());
        userImageAssetFileUploadTaskRepository = new UserImageAssetFileUploadTaskRepository(
                visionService.getFirebaseDatabase());
        documentRepository = new DocumentRepository(visionService.getContext().getContentResolver());
        assetCacheRepository = new AssetCacheRepository(visionService.getContext());
    }

    public void registerUserImageAssetFileUploadTask(@NonNull String assetId, @NonNull Uri imageUri) {
        ImageAssetFileUploadTask task = new ImageAssetFileUploadTask();
        task.setId(assetId);
        task.setUserId(CurrentUser.getInstance().getUserId());
        task.setInstanceId(FirebaseInstanceId.getInstance().getId());
        task.setSourceUriString(imageUri.toString());
        userImageAssetFileUploadTaskRepository.save(task);
    }

    @NonNull
    public TypedQuery<ImageAssetFileUploadTask> findAllImageAssetFileUploadTasks() {
        return userImageAssetFileUploadTaskRepository.findAll(CurrentUser.getInstance().getUserId());
    }

    @Nullable
    public File findAssetCacheFile(@NonNull String assetId) {
        return assetCacheRepository.find(assetId);
    }

    @NonNull
    public File findOrCreateAssetCacheFile(@NonNull String assetId) throws IOException {
        return assetCacheRepository.findOrCreate(assetId);
    }

    public void getCachedAssetFile(@NonNull String assetId, @NonNull String assetType,
                                   @Nullable OnSuccessListener<File> onSuccessListener,
                                   @Nullable OnFailureListener onFailureListener,
                                   @Nullable OnProgressListener onProgressListener) {
        final File file = assetCacheRepository.find(assetId);
        if (file == null) {
            // Download the file if it is not cached.
            try {
                final File destination = assetCacheRepository.findOrCreate(assetId);
                final FileDownloadTask downloadTask;
                if (ImageAsset.TYPE.equals(assetType)) {
                    downloadTask = userImageAssetFileRepository.download(
                            CurrentUser.getInstance().getUserId(), assetId, destination);
                } else {
                    throw new IllegalArgumentException("An unexpected value of 'assetType': " + assetType);
                }

                downloadTask.addOnSuccessListener(aVoid -> {
                    if (onSuccessListener != null) onSuccessListener.onSuccess(destination);
                });
                if (onFailureListener != null) {
                    downloadTask.addOnFailureListener(onFailureListener::onFailure);
                }
                if (onProgressListener != null) {
                    downloadTask.addOnProgressListener(snapshot -> {
                        onProgressListener.onProgress(snapshot.getTotalByteCount(), snapshot.getBytesTransferred());
                    });
                }
            } catch (IOException e) {
                if (onFailureListener != null) onFailureListener.onFailure(e);
            }
        } else {
            if (onSuccessListener != null) onSuccessListener.onSuccess(file);
        }
    }
}
