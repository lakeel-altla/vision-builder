package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Queue;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.ImageAsset;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

public final class AssetModelLoader implements Disposable {

    private static final Log LOG = LogFactory.getLog(AssetModelLoader.class);

    private final SimpleArrayMap<String, Loader> loaderMap = new SimpleArrayMap<>();

    // TODO: Consider caching models with its max capacity.
    private final SimpleArrayMap<String, Model> modelMap = new SimpleArrayMap<>();

    private final Queue<Task> taskQueue = new Queue<>();

    private final AssetCacheLoader assetCacheLoader;

    private boolean loading;

    public AssetModelLoader(@NonNull AssetCacheLoader assetCacheLoader) {
        this.assetCacheLoader = assetCacheLoader;
        loaderMap.put(ImageAsset.TYPE, new ImageAssetModelLoader());
    }

    @Override
    public void dispose() {
        for (int i = 0; i < modelMap.size(); i++) {
            modelMap.valueAt(i).dispose();
        }
        modelMap.clear();
    }

    public void run() {
        if (loading) return;

        if (0 < taskQueue.size) {
            final Task task = taskQueue.removeFirst();
            task.run();

            // TODO: Control the max load count.
            if (!loading) run();
        }
    }

    public void addTask(@NonNull String assetId, @NonNull String assetType,
                        @Nullable OnSuccessListener<Model> onSuccessListener,
                        @Nullable OnFailureListener onFailureListener) {
        taskQueue.addLast(new Task(assetId, assetType, onSuccessListener, onFailureListener));
    }

    private final class Task {

        @NonNull
        final String assetId;

        @NonNull
        final String assetType;

        @Nullable
        final OnSuccessListener<Model> onSuccessListener;

        @Nullable
        final OnFailureListener onFailureListener;

        Task(@NonNull String assetId, @NonNull String assetType,
             @Nullable OnSuccessListener<Model> onSuccessListener, @Nullable OnFailureListener onFailureListener) {
            this.assetId = assetId;
            this.assetType = assetType;
            this.onSuccessListener = onSuccessListener;
            this.onFailureListener = onFailureListener;
        }

        void run() {
            loading = true;

            final Model model = modelMap.get(assetId);
            if (model != null) {
                loading = false;
                if (onSuccessListener != null) onSuccessListener.onSuccess(model);
            } else {
                // TODO: public assets?
                assetCacheLoader.loadUserAssetCache(assetId, assetType, file -> {
                    final FileHandle fileHandle = Gdx.files.absolute(file.getPath());
                    final Loader loader = loaderMap.get(assetType);
                    if (loader == null) {
                        throw new IllegalStateException("An asset type is not supported: assetType = " + assetType);
                    }

                    loader.load(fileHandle, m -> {
                        LOG.d("An asset model is loaded: assetId = %s, assetType = %s", assetId, assetType);
                        modelMap.put(assetId, m);
                        loading = false;
                        if (onSuccessListener != null) onSuccessListener.onSuccess(m);
                    }, e -> {
                        loading = false;
                        if (onFailureListener != null) onFailureListener.onFailure(e);
                    });
                }, e -> {
                    loading = false;
                    if (onFailureListener != null) onFailureListener.onFailure(e);
                }, null);
            }
        }
    }

    private interface Loader {

        void load(@NonNull FileHandle assetFileHandle,
                  @Nullable OnSuccessListener<Model> onSuccessListener,
                  @Nullable OnFailureListener onFailureListener);
    }

    private static final class ImageAssetModelLoader implements Loader {

        // The texture scalling factor: 512 pixels = 1 meter.
        private static final float SCALING_FACTOR = 1f / 512f;

        private final TextureLoader textureLoader = new TextureLoader();

        @Override
        public void load(@NonNull FileHandle assetFileHandle,
                         @Nullable OnSuccessListener<Model> onSuccessListener,
                         @Nullable OnFailureListener onFailureListener) {
            textureLoader.load(assetFileHandle, texture -> {
                // On the graphics thread.
                final Material material = new Material(TextureAttribute.createDiffuse(texture),
                                                       new BlendingAttribute(),
                                                       IntAttribute.createCullFace(GL20.GL_NONE));

                final ModelBuilder modelBuilder = new ModelBuilder();
                final Model model = modelBuilder.createRect(
                        // 00: bottom left
                        -0.5f, -0.5f, 0f,
                        // 10: bottom right
                        0.5f, -0.5f, 0f,
                        // 11: top right
                        0.5f, 0.5f, 0f,
                        // 01: top left
                        -0.5f, 0.5f, 0f,
                        // normal
                        0, 1, 0,
                        material,
                        Position | Normal | TextureCoordinates);

                final float width = texture.getWidth() * SCALING_FACTOR;
                final float height = texture.getHeight() * SCALING_FACTOR;
                model.meshes.get(0).scale(width, height, 1);

                if (onSuccessListener != null) onSuccessListener.onSuccess(model);
            }, onFailureListener);
        }
    }
}
