package com.lakeel.altla.vision.builder.presentation.graphics.loader;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

final class ImageAssetModelLoader extends AssetLoader<Model> {

    // The texture scalling factor: 512 pixels = 1 meter.
    private static final float SCALING_FACTOR = 1f / 512f;

    private final AssetLoader<Texture> textureLoader;

    ImageAssetModelLoader(@NonNull AssetLoaderContext context, @NonNull AssetLoader<Texture> textureLoader) {
        super(context);
        this.textureLoader = textureLoader;
    }

    @Override
    protected void loadCore(@NonNull String assetId,
                            @Nullable OnSuccessListener<Model> onSuccessListener,
                            @Nullable OnFailureListener onFailureListener,
                            @Nullable OnProgressListener onProgressListener) {
        textureLoader.load(assetId, texture -> {
            // On the graphics thread.
            try {
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
            } catch (RuntimeException e) {
                if (onFailureListener != null) onFailureListener.onFailure(e);
            }
        }, e -> {
            if (onFailureListener != null) onFailureListener.onFailure(e);
        }, (totalBytes, bytesTransferred) -> {
            if (onProgressListener != null) onProgressListener.onProgress(totalBytes, bytesTransferred);
        });
    }
}
