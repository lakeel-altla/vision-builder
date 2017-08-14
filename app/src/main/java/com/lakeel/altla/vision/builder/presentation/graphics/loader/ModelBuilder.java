package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.ImageAsset;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import java.io.File;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

final class ModelBuilder extends AssetBuilder {

    private final SimpleArrayMap<String, TypedBuilder> typedBuilderMap = new SimpleArrayMap<>();

    ModelBuilder() {
        typedBuilderMap.put(ImageAsset.TYPE, new ImageAssetBuilder());
    }

    @Override
    Class<?> getTargetType() {
        return Model.class;
    }

    @Override
    void build(@NonNull String assetId, @NonNull String assetType, @NonNull File assetFile,
               @Nullable OnSuccessListener<Object> onSuccessListener, @Nullable OnFailureListener onFailureListener) {

        final TypedBuilder builder = typedBuilderMap.get(assetType);
        if (builder == null) {
            throw new IllegalArgumentException("The value of 'assetType' is not supported: assetType = " + assetType);
        }

        if (builder.context == null) {
            builder.context = context;
        }

        builder.build(assetId, assetType, assetFile, onSuccessListener, onFailureListener);
    }

    private static abstract class TypedBuilder {

        AssetBuilderContext context;

        abstract void build(@NonNull String assetId, @NonNull String assetType, @NonNull File assetFile,
                            @Nullable OnSuccessListener<Object> onSuccessListener,
                            @Nullable OnFailureListener onFailureListener);
    }

    private static final class ImageAssetBuilder extends TypedBuilder {

        // The texture scalling factor: 512 pixels = 1 meter.
        static final float SCALING_FACTOR = 1f / 512f;

        final com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
                modelBuilder = new com.badlogic.gdx.graphics.g3d.utils.ModelBuilder();

        @Override
        public void build(@NonNull String assetId, @NonNull String assetType, @NonNull File assetFile,
                          @Nullable OnSuccessListener<Object> onSuccessListener,
                          @Nullable OnFailureListener onFailureListener) {

            context.load(Texture.class, assetId, assetType, texture -> {
                Gdx.app.postRunnable(() -> {
                    try {
                        final Material material = new Material(TextureAttribute.createDiffuse(texture),
                                                               new BlendingAttribute(),
                                                               IntAttribute.createCullFace(GL20.GL_NONE));

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

                        // Callback on the loader thread.
                        if (onSuccessListener != null) {
                            context.runOnLoaderThread(() -> onSuccessListener.onSuccess(model));
                        }
                    } catch (RuntimeException e) {
                        // Callback on the loader thread.
                        if (onFailureListener != null) {
                            context.runOnLoaderThread(() -> onFailureListener.onFailure(e));
                        }
                    }
                });
            }, onFailureListener);
        }
    }
}
