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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private final AssetFileLoader assetFileLoader = new AssetFileLoader();

    public void loadAssetFile(@NonNull String assetId,
                              @Nullable OnSuccessListener<File> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener,
                              @Nullable OnProgressListener onProgressListener) {
        assetFileLoader.queue(assetId, onSuccessListener, onFailureListener, onProgressListener);
    }

    private final class AssetFileLoader {

        final HandlerThread loaderHandlerThread = new HandlerThread("AssetFileLoader");

        final Handler loaderHandler;

        final Handler uiHandler = new Handler(Looper.getMainLooper());

        final SimpleArrayMap<String, Task> taskMap = new SimpleArrayMap<>();

        AssetFileLoader() {
            loaderHandlerThread.start();
            loaderHandler = new Handler(loaderHandlerThread.getLooper());
        }

        void queue(@NonNull String assetId,
                   @Nullable OnSuccessListener<File> onSuccessListener,
                   @Nullable OnFailureListener onFailureListener,
                   @Nullable OnProgressListener onProgressListener) {
            synchronized (taskMap) {
                final Task newTask = new Task(assetId, onSuccessListener, onFailureListener, onProgressListener);
                final Task activeTask = taskMap.get(assetId);
                if (activeTask == null) {
                    taskMap.put(assetId, newTask);

                    LOG.d("A new task is activated: assetId = %s", assetId);

                    loaderHandler.post(newTask);
                } else {
                    LOG.d("An active task already exists: assetId = %s", assetId);

                    if (activeTask.sameAssetTasks == null) {
                        activeTask.sameAssetTasks = new ArrayList<>();
                    }
                    activeTask.sameAssetTasks.add(newTask);
                }
            }
        }

        private final class Task implements Runnable {

            final String assetId;

            @Nullable
            final OnSuccessListener<File> onSuccessListener;

            @Nullable
            final OnFailureListener onFailureListener;

            @Nullable
            final OnProgressListener onProgressListener;

            @Nullable
            List<Task> sameAssetTasks;

            Task(@NonNull String assetId,
                 @Nullable OnSuccessListener<File> onSuccessListener, @Nullable OnFailureListener onFailureListener,
                 @Nullable OnProgressListener onProgressListener) {
                this.assetId = assetId;
                this.onSuccessListener = onSuccessListener;
                this.onFailureListener = onFailureListener;
                this.onProgressListener = onProgressListener;
            }

            @Override
            public void run() {
                final File file = assetCacheRepository.find(assetId);
                if (file == null) {
                    LOG.d("The asset file is not downloaded and cached yet: assetId = %s", assetId);

                    try {
                        final File destination = assetCacheRepository.findOrCreate(assetId);
                        final FileDownloadTask downloadTask = userAssetFileRepository.download(
                                CurrentUser.getInstance().getUserId(), assetId, destination);

                        downloadTask.addOnSuccessListener(aVoid -> {
                            // The UI thread.
                            if (onSuccessListener != null) {
                                onSuccessListener.onSuccess(destination);
                            }
                            synchronized (taskMap) {
                                if (sameAssetTasks != null) {
                                    for (final Task task : sameAssetTasks) {
                                        if (task.onSuccessListener != null) {
                                            task.onSuccessListener.onSuccess(destination);
                                        }
                                    }
                                }
                                taskMap.remove(assetId);
                            }
                        });

                        downloadTask.addOnFailureListener(e -> {
                            // The UI thread.
                            if (onFailureListener != null) {
                                onFailureListener.onFailure(e);
                            }
                            synchronized (taskMap) {
                                if (sameAssetTasks != null) {
                                    for (final Task task : sameAssetTasks) {
                                        if (task.onFailureListener != null) {
                                            task.onFailureListener.onFailure(e);
                                        }
                                    }
                                }
                                taskMap.remove(assetId);
                            }
                        });

                        downloadTask.addOnProgressListener(snapshot -> {
                            // The UI thread.
                            final long totalBytes = snapshot.getTotalByteCount();
                            final long bytesTransferred = snapshot.getBytesTransferred();
                            if (onProgressListener != null) {
                                onProgressListener.onProgress(totalBytes, bytesTransferred);
                            }
                            synchronized (taskMap) {
                                if (sameAssetTasks != null) {
                                    for (final Task task : sameAssetTasks) {
                                        if (task.onProgressListener != null) {
                                            task.onProgressListener.onProgress(totalBytes, bytesTransferred);
                                        }
                                    }
                                }
                            }
                        });
                    } catch (IOException e) {
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    }
                } else {
                    LOG.d("The asset file is already downloaded and cached: assetId = %s", assetId);

                    // The loader thread.
                    uiHandler.post(() -> {
                        // The UI thread.
                        if (onSuccessListener != null) onSuccessListener.onSuccess(file);
                        synchronized (taskMap) {
                            if (sameAssetTasks != null) {
                                for (final Task task : sameAssetTasks) {
                                    if (task.onSuccessListener != null) {
                                        task.onSuccessListener.onSuccess(file);
                                    }
                                }
                            }
                            taskMap.remove(assetId);
                        }
                    });
                }
            }
        }
    }
}
