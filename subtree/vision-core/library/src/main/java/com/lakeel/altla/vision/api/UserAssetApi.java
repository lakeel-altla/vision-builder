package com.lakeel.altla.vision.api;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.data.repository.android.AssetCacheRepository;
import com.lakeel.altla.vision.data.repository.android.DocumentRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserAssetFileRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserAssetFileUploadTaskRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserImageAssetRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.AssetFileUploadTask;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public final class UserAssetApi extends BaseVisionApi {

    private static final Log LOG = LogFactory.getLog(UserAssetApi.class);

    private final UserImageAssetRepository userImageAssetRepository;

    private final UserAssetFileRepository userAssetFileRepository;

    private final UserAssetFileUploadTaskRepository userAssetFileUploadTaskRepository;

    private final DocumentRepository documentRepository;

    private final AssetCacheRepository assetCacheRepository;

    public UserAssetApi(@NonNull VisionService visionService) {
        super(visionService);

        userImageAssetRepository = new UserImageAssetRepository(visionService.getFirebaseDatabase());
        userAssetFileRepository = new UserAssetFileRepository(visionService.getFirebaseStorage());
        userAssetFileUploadTaskRepository = new UserAssetFileUploadTaskRepository(
                visionService.getFirebaseDatabase());
        documentRepository = new DocumentRepository(visionService.getContext().getContentResolver());
        assetCacheRepository = new AssetCacheRepository(visionService.getContext());
    }

    public void registerUserAssetFileUploadTask(@NonNull String assetId, @NonNull Uri sourceUri) {
        final AssetFileUploadTask task = new AssetFileUploadTask();
        task.setId(assetId);
        task.setUserId(CurrentUser.getInstance().getUserId());
        task.setInstanceId(FirebaseInstanceId.getInstance().getId());
        task.setSourceUriString(sourceUri.toString());
        userAssetFileUploadTaskRepository.save(task);
    }

    @NonNull
    public TypedQuery<AssetFileUploadTask> findAllAssetFileUploadTasks() {
        return userAssetFileUploadTaskRepository.findAll(CurrentUser.getInstance().getUserId());
    }

    @Nullable
    public File findAssetCacheFile(@NonNull String assetId) {
        return assetCacheRepository.find(assetId);
    }

    @NonNull
    public File findOrCreateAssetCacheFile(@NonNull String assetId) throws IOException {
        return assetCacheRepository.findOrCreate(assetId);
    }

    public void loadAssetFile(@NonNull String assetId,
                              @Nullable OnSuccessListener<File> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener,
                              @Nullable OnProgressListener onProgressListener) {

        final File file = assetCacheRepository.find(assetId);
        if (file == null) {
            LOG.d("Downloading the asset file: assetId = %s", assetId);

            try {
                final File destination = assetCacheRepository.findOrCreate(assetId);
                final FileDownloadTask downloadTask = userAssetFileRepository.download(
                        CurrentUser.getInstance().getUserId(), assetId, destination);

                downloadTask.addOnSuccessListener(aVoid -> {
                    if (onSuccessListener != null) {
                        onSuccessListener.onSuccess(destination);
                    }
                });

                downloadTask.addOnFailureListener(e -> {
                    if (onFailureListener != null) {
                        onFailureListener.onFailure(e);
                    }
                });

                downloadTask.addOnProgressListener(snapshot -> {
                    final long totalBytes = snapshot.getTotalByteCount();
                    final long bytesTransferred = snapshot.getBytesTransferred();
                    if (onProgressListener != null) {
                        onProgressListener.onProgress(totalBytes, bytesTransferred);
                    }
                });
            } catch (IOException e) {
                if (onFailureListener != null) onFailureListener.onFailure(e);
            }
        } else {
            LOG.d("Using the cached asset file: assetId = %s", assetId);

            if (onSuccessListener != null) {
                onSuccessListener.onSuccess(file);
            }
        }
    }
}
