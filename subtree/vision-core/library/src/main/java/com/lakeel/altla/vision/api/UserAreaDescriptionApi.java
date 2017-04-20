package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.android.AreaDescriptionCacheRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserAreaDescriptionFileRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserAreaDescriptionRepository;
import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.ObservableList;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.AreaDescription;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public final class UserAreaDescriptionApi extends BaseVisionApi {

    private final UserAreaDescriptionRepository userAreaDescriptionRepository;

    private final AreaDescriptionCacheRepository areaDescriptionCacheRepository;

    private final UserAreaDescriptionFileRepository userAreaDescriptionFileRepository;

    UserAreaDescriptionApi(@NonNull VisionService visionService) {
        super(visionService);

        userAreaDescriptionRepository = new UserAreaDescriptionRepository(visionService.getFirebaseDatabase());
        areaDescriptionCacheRepository = new AreaDescriptionCacheRepository(Environment.getExternalStorageDirectory());
        userAreaDescriptionFileRepository = new UserAreaDescriptionFileRepository(visionService.getFirebaseStorage());
    }

    public void findAreaDescriptionById(@NonNull String areaDescriptionId,
                                        @Nullable OnSuccessListener<AreaDescription> onSuccessListener,
                                        @Nullable OnFailureListener onFailureListener) {
        userAreaDescriptionRepository.find(CurrentUser.getInstance().getUserId(), areaDescriptionId,
                                           onSuccessListener, onFailureListener);
    }

    public void findAreaDescriptionsByAreaId(@NonNull String areaId,
                                             @Nullable OnSuccessListener<List<AreaDescription>> onSuccessListener,
                                             @Nullable OnFailureListener onFailureListener) {
        userAreaDescriptionRepository.findByAreaId(CurrentUser.getInstance().getUserId(), areaId,
                                                   onSuccessListener, onFailureListener);
    }

    @NonNull
    public ObservableData<AreaDescription> observeAreaDescriptionById(@NonNull String areaDescriptionId) {
        return userAreaDescriptionRepository.observe(CurrentUser.getInstance().getUserId(), areaDescriptionId);
    }

    @NonNull
    public ObservableList<AreaDescription> observeAreaDescriptionsByAreaId(@NonNull String areaId) {
        return userAreaDescriptionRepository.observeByAreaId(CurrentUser.getInstance().getUserId(), areaId);
    }

    @NonNull
    public ObservableList<AreaDescription> observeAllAreaDescriptions() {
        return userAreaDescriptionRepository.observeAll(CurrentUser.getInstance().getUserId());
    }

    public void saveAreaDescriptionById(@NonNull AreaDescription areaDescription) {
        if (!CurrentUser.getInstance().getUserId().equals(areaDescription.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userAreaDescriptionRepository.save(areaDescription);
    }

    public void deleteAreaDescriptionById(@NonNull String areaDescriptionId) {
        userAreaDescriptionRepository.delete(CurrentUser.getInstance().getUserId(), areaDescriptionId);
    }

    @NonNull
    public File getAreaDescriptionCacheById(@NonNull String areaDescriptionId) {
        return areaDescriptionCacheRepository.getFile(areaDescriptionId);
    }

    public void deleteAreaDescriptionCacheById(@NonNull String areaDescriptionId) {
        areaDescriptionCacheRepository.delete(areaDescriptionId);
    }

    @NonNull
    public File getAreaDescriptionCacheDirectory() {
        return areaDescriptionCacheRepository.getDirectory();
    }

    public void uploadAreaDescription(@NonNull String areaDescriptionId,
                                      @Nullable OnSuccessListener<Void> onSuccessListener,
                                      @Nullable OnFailureListener onFailureListener,
                                      @Nullable OnProgressListener onProgressListener) {

        File file = areaDescriptionCacheRepository.getFile(areaDescriptionId);

        try {
            FileInputStream stream = new FileInputStream(file);
            int totalBytes = stream.available();
            userAreaDescriptionFileRepository.upload(
                    CurrentUser.getInstance().getUserId(), areaDescriptionId, stream,
                    aVoid -> {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            getLog().e("", e);
                        }
                        if (onSuccessListener != null) onSuccessListener.onSuccess(null);
                    }, e -> {
                        try {
                            stream.close();
                        } catch (IOException ex) {
                            getLog().e("", ex);
                        }
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    }, (_totalBytes, bytesTransferred) -> {
                        if (onProgressListener != null) {
                            onProgressListener.onProgress(totalBytes, bytesTransferred);
                        }
                    });
        } catch (IOException e) {
            if (onFailureListener != null) onFailureListener.onFailure(e);
        }
    }

    public void downloadAreaDescription(@NonNull String areaDescriptionId,
                                        @Nullable OnSuccessListener<Void> onSuccessListener,
                                        @Nullable OnFailureListener onFailureListener,
                                        @Nullable OnProgressListener onProgressListener) {
        File file = areaDescriptionCacheRepository.getFile(areaDescriptionId);
        userAreaDescriptionFileRepository.download(
                CurrentUser.getInstance().getUserId(), areaDescriptionId, file,
                onSuccessListener, onFailureListener, onProgressListener);
    }
}
