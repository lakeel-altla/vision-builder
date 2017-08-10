package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import java.io.File;

public final class AssetLoader implements AssetBuilderContext {

    private static final Log LOG = LogFactory.getLog(AssetLoader.class);

    private final HandlerThread handlerThread = new HandlerThread(AssetLoader.class.getSimpleName());

    private final Handler handler;

    private final VisionService visionService;

    private final SimpleArrayMap<Class<?>, SimpleArrayMap<String, Task<?>>> taskMap = new SimpleArrayMap<>();

    private final SimpleArrayMap<Class<?>, AssetBuilder> assetBuilderMap = new SimpleArrayMap<>();

    public AssetLoader(@NonNull VisionService visionService) {
        this.visionService = visionService;

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        assetBuilderMap.put(Texture.class, new TextureBuilder(this));
        assetBuilderMap.put(Model.class, new ModelBuilder(this));

        taskMap.put(Texture.class, new SimpleArrayMap<>());
        taskMap.put(Model.class, new SimpleArrayMap<>());
    }

    @Override
    public <T> void load(@NonNull Class<T> clazz, @NonNull String assetId, @NonNull String assetType,
                         @Nullable OnSuccessListener<T> onSuccessListener,
                         @Nullable OnFailureListener onFailureListener) {

        // This method will be invoked on any threads.

        final AssetBuilder assetBuilder = assetBuilderMap.get(clazz);
        if (assetBuilder == null) {
            throw new IllegalArgumentException("The value of 'clazz' is not supported: clazz = " + clazz);
        }

        synchronized (taskMap) {
            final Task<T> newTask = new Task<>(clazz, assetId, assetType, assetBuilder,
                                               onSuccessListener, onFailureListener);

            final SimpleArrayMap<String, Task<?>> classTaskMap = taskMap.get(clazz);

            @SuppressWarnings("unchecked")
            final Task<T> activeTask = (Task<T>) classTaskMap.get(assetId);

            if (activeTask == null) {
                classTaskMap.put(assetId, newTask);

                LOG.d("A new task is activated: %s", newTask);

                // Run tasks on the loader thread.
                handler.post(newTask);
            } else {
                LOG.d("An active task already exists: %s", newTask);

                if (activeTask.sameAssetTasks == null) {
                    activeTask.sameAssetTasks = new Array<>();
                }
                activeTask.sameAssetTasks.add(newTask);
            }
        }
    }

    private void loadAssetFile(@NonNull String assetId,
                               @Nullable OnSuccessListener<File> onSuccessListener,
                               @Nullable OnFailureListener onFailureListener) {

        LOG.v("Loading an asset file: assetId = %s", assetId);

        // TODO: how should we load public assets?
        visionService.getUserAssetApi()
                     .loadAssetFile(assetId, assetFile -> {
                         LOG.v("Loaded an asset file: assetId = %s, assetFile = %s", assetId, assetFile);
                         if (onSuccessListener != null) {
                             handler.post(() -> onSuccessListener.onSuccess(assetFile));
                         }
                     }, e -> {
                         LOG.e("Failed to load an asset file: assetId = %s", assetId, e);
                         if (onFailureListener != null) {
                             handler.post(() -> onFailureListener.onFailure(e));
                         }
                     }, null);
    }

    private final class Task<T> implements Runnable {

        final Class<T> clazz;

        final String assetId;

        final String assetType;

        final AssetBuilder assetBuilder;

        final OnSuccessListener<T> onSuccessListener;

        final OnFailureListener onFailureListener;

        Array<Task<T>> sameAssetTasks;

        Task(@NonNull Class<T> clazz, @NonNull String assetId, @NonNull String assetType,
             @NonNull AssetBuilder assetBuilder,
             @Nullable OnSuccessListener<T> onSuccessListener,
             @Nullable OnFailureListener onFailureListener) {

            this.clazz = clazz;
            this.assetId = assetId;
            this.assetType = assetType;
            this.assetBuilder = assetBuilder;
            this.onSuccessListener = onSuccessListener;
            this.onFailureListener = onFailureListener;
        }

        @Override
        public void run() {
            // This method will be invoked on the loader thread.

            LOG.v("Starting the task: %s", this);

            // Callbacks of the loadAssetFile() method will be invoked on the loader thread.
            loadAssetFile(assetId, assetFile -> {
                LOG.v("Building an asset representation: assetFile = %s", assetFile);

                // Callbacks of the build() method will be invoked on the graphics thread.
                assetBuilder.build(assetId, assetType, assetFile, result -> {
                    LOG.v("Built an asset representation: assetFile = %s", assetFile);

                    synchronized (taskMap) {
                        taskMap.get(clazz).remove(assetId);
                    }

                    @SuppressWarnings("unchecked")
                    final T typedResult = (T) result;

                    if (onSuccessListener != null) {
                        onSuccessListener.onSuccess(typedResult);
                    }

                    if (sameAssetTasks != null) {
                        for (final Task<T> task : sameAssetTasks) {
                            if (task.onSuccessListener != null) {
                                task.onSuccessListener.onSuccess(typedResult);
                            }
                        }
                    }
                }, e -> {
                    LOG.e("Failed to build an asset representation: assetFile = %s", assetFile);

                    synchronized (taskMap) {
                        taskMap.get(clazz).remove(assetId);
                    }

                    if (onFailureListener != null) {
                        Gdx.app.postRunnable(() -> onFailureListener.onFailure(e));
                    }

                    if (sameAssetTasks != null) {
                        for (final Task task : sameAssetTasks) {
                            if (task.onFailureListener != null) {
                                task.onFailureListener.onFailure(e);
                            }
                        }
                    }
                });
            }, e -> {
                synchronized (taskMap) {
                    taskMap.get(clazz).remove(assetId);
                }

                if (onFailureListener != null) {
                    Gdx.app.postRunnable(() -> onFailureListener.onFailure(e));
                }

                if (sameAssetTasks != null) {
                    Gdx.app.postRunnable(() -> {
                        for (final Task task : sameAssetTasks) {
                            if (task.onFailureListener != null) {
                                task.onFailureListener.onFailure(e);
                            }
                        }
                    });
                }
            });
        }

        @Override
        public String toString() {
            return "[class = " + clazz + ", assetId = " + assetId + ", assetType = " + assetType + "]";
        }
    }
}
