package com.lakeel.altla.vision.api;

import com.google.firebase.iid.FirebaseInstanceId;

import com.lakeel.altla.vision.data.repository.android.DocumentRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserImageAssetFileRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserImageAssetFileUploadTaskRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserImageAssetRepository;
import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.ObservableList;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.model.ImageAssetFileUploadTask;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class UserAssetApi extends BaseVisionApi {

    private final UserImageAssetRepository userImageAssetRepository;

    private final UserImageAssetFileRepository userImageAssetFileRepository;

    private final UserImageAssetFileUploadTaskRepository userImageAssetFileUploadTaskRepository;

    private final DocumentRepository documentRepository;

    public UserAssetApi(@NonNull VisionService visionService) {
        super(visionService);

        userImageAssetRepository = new UserImageAssetRepository(visionService.getFirebaseDatabase());
        userImageAssetFileRepository = new UserImageAssetFileRepository(visionService.getFirebaseStorage());
        userImageAssetFileUploadTaskRepository = new UserImageAssetFileUploadTaskRepository(
                visionService.getFirebaseDatabase());
        documentRepository = new DocumentRepository(visionService.getContext().getContentResolver());
    }

    public void findUserImageAssetById(@NonNull String assetId,
                                       @Nullable OnSuccessListener<ImageAsset> onSuccessListener,
                                       @Nullable OnFailureListener onFailureListener) {
        userImageAssetRepository.find(CurrentUser.getInstance().getUserId(), assetId,
                                      onSuccessListener, onFailureListener);
    }

    @NonNull
    public ObservableData<ImageAsset> observeUserImageAssetById(@NonNull String assetId) {
        return userImageAssetRepository.observe(CurrentUser.getInstance().getUserId(), assetId);
    }

    @NonNull
    public ObservableList<ImageAsset> observeAllUserImageAssets() {
        return userImageAssetRepository.observeAll(CurrentUser.getInstance().getUserId());
    }

    public void saveUserImageAsset(@NonNull ImageAsset asset) {
        if (!CurrentUser.getInstance().getUserId().equals(asset.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userImageAssetRepository.save(asset);
    }

    @NonNull
    public void getUserImageAssetFileUriById(@NonNull String assetId,
                                             @Nullable OnSuccessListener<Uri> onSuccessListener,
                                             @Nullable OnFailureListener onFailureListener) {
        userImageAssetFileRepository.getDownloadUri(CurrentUser.getInstance().getUserId(), assetId,
                                                    onSuccessListener, onFailureListener);
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
    public ObservableList<ImageAssetFileUploadTask> observeUserImageAssetFileUploadTask() {
        return userImageAssetFileUploadTaskRepository.observeAll(CurrentUser.getInstance().getUserId());
    }

    public void uploadUserImageAssetFile(@NonNull ImageAssetFileUploadTask task,
                                         @Nullable OnSuccessListener<Void> onSuccessListener,
                                         @Nullable OnFailureListener onFailureListener,
                                         @Nullable OnProgressListener onProgressListener) {
        if (!CurrentUser.getInstance().getUserId().equals(task.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }
        if (task.getSourceUriString() == null) {
            throw new IllegalArgumentException("The source URI is required.");
        }

        userImageAssetRepository.find(task.getUserId(), task.getId(), asset -> {
            if (asset == null) {
                throw new IllegalStateException(String.format("Entity not found: assetId = %s", task.getId()));
            }

            try {
                InputStream inputStream = new BufferedInputStream(
                        documentRepository.openInputStream(task.getSourceUriString()));

                long totalBytes = inputStream.available();

                userImageAssetFileRepository.upload(task.getUserId(), task.getId(), inputStream, aVoid -> {
                    // Uploaded.

                    // Update the status.
                    asset.setFileUploaded(true);
                    userImageAssetRepository.save(asset);

                    // Delete the task.
                    userImageAssetFileUploadTaskRepository.delete(task.getUserId(), task.getId());

                    try {
                        inputStream.close();
                    } catch (IOException closeFailed) {
                        getLog().e("Failed to close the stream.", closeFailed);
                    }

                    if (onSuccessListener != null) onSuccessListener.onSuccess(null);
                }, ex -> {
                    // Failed.
                    try {
                        inputStream.close();
                    } catch (IOException closeFailed) {
                        getLog().e("Failed to close the stream.", closeFailed);
                    }

                    if (onFailureListener != null) onFailureListener.onFailure(ex);
                }, (_totalBytes, bytesTransferred) -> {
                    if (onProgressListener != null) onProgressListener.onProgress(totalBytes, bytesTransferred);
                });
            } catch (IOException ex) {
                if (onFailureListener != null) onFailureListener.onFailure(ex);
            }
        }, onFailureListener);
    }
}
