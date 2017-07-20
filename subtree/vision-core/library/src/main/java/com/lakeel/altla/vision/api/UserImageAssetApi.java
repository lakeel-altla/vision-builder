package com.lakeel.altla.vision.api;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class UserImageAssetApi extends BaseVisionApi {

    private final AssetCacheRepository assetCacheRepository;

    private final DocumentRepository documentRepository;

    private final UserImageAssetRepository userImageAssetRepository;

    private final UserImageAssetFileRepository userImageAssetFileRepository;

    private final UserImageAssetFileUploadTaskRepository userImageAssetFileUploadTaskRepository;

    public UserImageAssetApi(@NonNull VisionService visionService) {
        super(visionService);
        assetCacheRepository = new AssetCacheRepository(visionService.getContext());
        documentRepository = new DocumentRepository(visionService.getContext().getContentResolver());
        userImageAssetRepository = new UserImageAssetRepository(visionService.getFirebaseDatabase());
        userImageAssetFileRepository = new UserImageAssetFileRepository(visionService.getFirebaseStorage());
        userImageAssetFileUploadTaskRepository =
                new UserImageAssetFileUploadTaskRepository(visionService.getFirebaseDatabase());
    }

    public TypedQuery<ImageAsset> findAllImageAssets() {
        return userImageAssetRepository.findAll(CurrentUser.getInstance().getUserId());
    }

    public void doImageAssetFileUploadTask(@NonNull ImageAssetFileUploadTask task,
                                           @Nullable OnSuccessListener<File> onSuccessListener,
                                           @Nullable OnFailureListener onFailureListener,
                                           @Nullable OnProgressListener onProgressListener) {
        if (!CurrentUser.getInstance().getUserId().equals(task.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }
        if (task.getSourceUriString() == null) {
            throw new IllegalArgumentException("The source URI is required.");
        }

        final String userId = task.getUserId();
        final String assetId = task.getId();
        final String sourceUriString = task.getSourceUriString();

        userImageAssetRepository
                .find(userId, assetId)
                .addListenerForSingleValue(new TypedQuery.TypedValueEventListener<ImageAsset>() {
                    @Override
                    public void onDataChange(@Nullable ImageAsset asset) {
                        if (asset == null) {
                            throw new IllegalStateException(
                                    String.format("The ImageAsset not found: id = %s", assetId));
                        }

                        try {
                            final InputStream stream = new BufferedInputStream(
                                    documentRepository.openInputStream(sourceUriString));

                            final long totalBytes = stream.available();

                            final UploadTask uploadTask =
                                    userImageAssetFileRepository.upload(userId, assetId, stream);

                            uploadTask.addOnSuccessListener(aVoid -> {
                                // Uploaded.

                                // Update the status.
                                asset.setFileUploaded(true);
                                userImageAssetRepository.save(asset);

                                // Delete the task.
                                userImageAssetFileUploadTaskRepository.delete(userId, assetId);

                                try {
                                    stream.close();
                                } catch (IOException closeFailed) {
                                    getLog().e("Failed to close the stream.", closeFailed);
                                }

                                if (onSuccessListener != null) {
                                    onSuccessListener.onSuccess(null);
                                }
                            });

                            if (onFailureListener != null) {
                                uploadTask.addOnFailureListener(onFailureListener::onFailure);
                            }

                            if (onProgressListener != null) {
                                uploadTask.addOnProgressListener(snapshot -> {
                                    onProgressListener.onProgress(totalBytes, snapshot.getBytesTransferred());
                                });
                            }
                        } catch (IOException e) {
                            if (onFailureListener != null) onFailureListener.onFailure(e);

                        }
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    }
                });
    }

    public void getCachedImageAssetFile(@NonNull String assetId,
                                        @Nullable OnSuccessListener<File> onSuccessListener,
                                        @Nullable OnFailureListener onFailureListener,
                                        @Nullable OnProgressListener onProgressListener) {
        final File file = assetCacheRepository.find(assetId);
        if (file == null) {
            // Download the file if it is not cached.
            try {
                final File destination = assetCacheRepository.findOrCreate(assetId);
                final FileDownloadTask downloadTask = userImageAssetFileRepository
                        .download(CurrentUser.getInstance().getUserId(), assetId, destination);

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
