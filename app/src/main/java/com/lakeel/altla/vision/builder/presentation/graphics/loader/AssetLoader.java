package com.lakeel.altla.vision.builder.presentation.graphics.loader;

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

    private final SimpleArrayMap<Class<?>, SimpleArrayMap<String, Object>> assetMap = new SimpleArrayMap<>();

    public AssetLoader(@NonNull VisionService visionService) {
        this.visionService = visionService;

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        register(TextureBuilder.class);
        register(ModelBuilder.class);
    }

    @Override
    public <T> void load(@NonNull Class<T> clazz, @NonNull String assetId, @NonNull String assetType,
                         @Nullable OnSuccessListener<T> onSuccessListener,
                         @Nullable OnFailureListener onFailureListener) {

        // This method will be invoked on the loader/graphics threads.

        final Task<T> newTask = new Task<>(clazz, assetId, assetType, onSuccessListener, onFailureListener);
        handler.post(newTask);
    }

    @Override
    public void runOnLoaderThread(@NonNull Runnable runnable) {
        handler.post(runnable);
    }

    public void register(@NonNull Class<? extends AssetBuilder> clazz) {
        try {
            final AssetBuilder builder = clazz.newInstance();
            final Class<?> targetType = builder.getTargetType();

            assetBuilderMap.put(targetType, builder);
            taskMap.put(targetType, new SimpleArrayMap<>());
            assetMap.put(targetType, new SimpleArrayMap<>());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final class Task<T> implements Runnable {

        final Class<T> clazz;

        final String assetId;

        final String assetType;


        final OnSuccessListener<T> onSuccessListener;

        final OnFailureListener onFailureListener;

        Array<Task<T>> sameAssetTasks;

        Task(@NonNull Class<T> clazz, @NonNull String assetId, @NonNull String assetType,
             @Nullable OnSuccessListener<T> onSuccessListener,
             @Nullable OnFailureListener onFailureListener) {

            this.clazz = clazz;
            this.assetId = assetId;
            this.assetType = assetType;
            this.onSuccessListener = onSuccessListener;
            this.onFailureListener = onFailureListener;
        }

        @Override
        public void run() {
            // This method will be invoked on the loader thread.

            try {
                LOG.v("Loading an asset: class = %s, assetId = %s", clazz, assetId);

                final SimpleArrayMap<String, Task<?>> classTaskMap = taskMap.get(clazz);
                if (classTaskMap == null) {
                    throw new IllegalStateException("The value of 'clazz' is not supported: clazz = " + clazz);
                }

                @SuppressWarnings("unchecked")
                final Task<T> activeTask = (Task<T>) classTaskMap.get(assetId);

                if (activeTask == null) {
                    classTaskMap.put(assetId, this);

                    load();
                } else {
                    if (activeTask.sameAssetTasks == null) {
                        activeTask.sameAssetTasks = new Array<>();
                    }

                    activeTask.sameAssetTasks.add(this);

                    LOG.v("Waiting a task loading the same asset: class = %s, assetId = %s", clazz, assetId);
                }

            } catch (RuntimeException e) {
                if (onFailureListener == null) {
                    LOG.e("A runtime error occured on the asset loader thread.", e);
                } else {
                    // Callback on the graphics thread.
                    onFailureListener.onFailure(e);
                }
            }
        }

        private void load() {
            // Check the cache.
            final SimpleArrayMap<String, Object> classAssetMap = assetMap.get(clazz);
            if (classAssetMap == null) {
                throw new IllegalStateException(
                        "The value of the field 'clazz' is not supported: clazz = " + clazz);
            }

            final Object asset = classAssetMap.get(assetId);
            if (asset != null) {
                LOG.v("Using the cache: class = %s, assetId = %s", clazz, assetId);

                taskMap.get(clazz).remove(assetId);

                @SuppressWarnings("unchecked")
                T typedResult = (T) asset;
                raiseOnSuccess(typedResult);

                return;
            }

            final AssetBuilder assetBuilder = assetBuilderMap.get(clazz);
            if (assetBuilder == null) {
                throw new IllegalArgumentException("The value of 'clazz' is not supported: clazz = " + clazz);
            }

            if (!assetBuilder.isInitialized()) {
                assetBuilder.initialize(AssetLoader.this);
            }

            // Callbacks of the loadAssetFile() method will be invoked on the loader thread.
            loadAssetFile(assetId, assetFile -> {
                assetBuilder.build(assetId, assetType, assetFile, result -> {
                    // Cache the asset.
                    assetMap.get(clazz).put(assetId, result);
                    taskMap.get(clazz).remove(assetId);

                    @SuppressWarnings("unchecked")
                    final T typedResult = (T) result;
                    raiseOnSuccess(typedResult);
                }, e -> {
                    taskMap.get(clazz).remove(assetId);
                    raiseOnFailure(e);
                });
            }, e -> {
                taskMap.get(clazz).remove(assetId);
                raiseOnFailure(e);
            });
        }

        private void loadAssetFile(@NonNull String assetId,
                                   @Nullable OnSuccessListener<File> onSuccessListener,
                                   @Nullable OnFailureListener onFailureListener) {

            LOG.v("Loading a file: assetId = %s", assetId);

            // TODO: how should we load public assets?
            visionService.getUserAssetApi()
                         .loadAssetFile(assetId, assetFile -> {
                             LOG.v("Loaded the file: assetId = %s, assetFile = %s", assetId, assetFile);
                             if (onSuccessListener != null) {
                                 handler.post(() -> onSuccessListener.onSuccess(assetFile));
                             }
                         }, e -> {
                             LOG.e("Failed to load the file: assetId = %s", assetId, e);
                             if (onFailureListener != null) {
                                 handler.post(() -> onFailureListener.onFailure(e));
                             }
                         }, null);
        }

        private void raiseOnSuccess(@NonNull T result) {
            LOG.v("Loaded the asset: class = %s, assetId = %s", clazz, assetId);

            if (onSuccessListener != null) {
                onSuccessListener.onSuccess(result);
            }

            if (sameAssetTasks != null) {
                for (final Task<T> task : sameAssetTasks) {
                    if (task.onSuccessListener != null) {
                        task.onSuccessListener.onSuccess(result);
                    }
                }
            }
        }

        private void raiseOnFailure(@NonNull Exception e) {
            LOG.e("Failed to load the asset: class = %s, assetId = %s", clazz, assetId);

            if (onFailureListener != null) {
                onFailureListener.onFailure(e);
            }

            if (sameAssetTasks != null) {
                for (final Task task : sameAssetTasks) {
                    if (task.onFailureListener != null) {
                        task.onFailureListener.onFailure(e);
                    }
                }
            }
        }
    }
}
