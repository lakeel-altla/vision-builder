package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.utils.Array;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

public abstract class AssetLoader<T> {

    private static final Log LOG = LogFactory.getLog(AssetLoader.class);

    protected final AssetLoaderContext context;

    private final SimpleArrayMap<String, Task> taskMap = new SimpleArrayMap<>();

    protected AssetLoader(@NonNull AssetLoaderContext context) {
        this.context = context;
    }

    public void load(@NonNull String assetId,
                     @Nullable OnSuccessListener<T> onSuccessListener,
                     @Nullable OnFailureListener onFailureListener,
                     @Nullable OnProgressListener onProgressListener) {
        synchronized (taskMap) {
            final Task newTask = new Task(assetId, onSuccessListener, onFailureListener, onProgressListener);
            final Task activeTask = taskMap.get(assetId);
            if (activeTask == null) {
                taskMap.put(assetId, newTask);
                LOG.d("A new task is activated: assetId = %s", assetId);
                context.runOnLoaderThread(newTask);
            } else {
                LOG.d("An active task already exists: assetid = %s", assetId);
                if (activeTask.sameAssetTasks == null) {
                    activeTask.sameAssetTasks = new Array<>();
                }
                activeTask.sameAssetTasks.add(newTask);
            }
        }
    }

    protected abstract void loadCore(@NonNull String assetId,
                                     @Nullable OnSuccessListener<T> onSuccessListener,
                                     @Nullable OnFailureListener onFailureListener,
                                     @Nullable OnProgressListener onProgressListener);

    private final class Task implements Runnable {

        final String assetId;

        final OnSuccessListener<T> onSuccessListener;

        final OnFailureListener onFailureListener;

        final OnProgressListener onProgressListener;

        Array<Task> sameAssetTasks;

        Task(@NonNull String assetId,
             @Nullable OnSuccessListener<T> onSuccessListener,
             @Nullable OnFailureListener onFailureListener,
             @Nullable OnProgressListener onProgressListener) {

            this.assetId = assetId;
            this.onSuccessListener = onSuccessListener;
            this.onFailureListener = onFailureListener;
            this.onProgressListener = onProgressListener;
        }

        @Override
        public void run() {
            loadCore(assetId, result -> {
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess(result);
                }
                synchronized (taskMap) {
                    if (sameAssetTasks != null) {
                        for (final Task task : sameAssetTasks) {
                            if (task.onSuccessListener != null) {
                                task.onSuccessListener.onSuccess(result);
                            }
                        }
                    }
                    taskMap.remove(assetId);
                }
            }, e -> {
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
            }, (totalBytes, bytesTransferred) -> {
                if (onProgressListener != null) {
                    onProgressListener.onProgress(totalBytes, bytesTransferred);
                }
                synchronized (taskMap) {
                    for (final Task task : sameAssetTasks) {
                        if (task.onProgressListener != null) {
                            task.onProgressListener.onProgress(totalBytes, bytesTransferred);
                        }
                    }
                }
            });
        }
    }
}
