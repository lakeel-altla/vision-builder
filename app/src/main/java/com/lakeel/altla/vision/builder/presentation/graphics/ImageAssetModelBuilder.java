package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import android.support.annotation.NonNull;

import java.io.File;

public final class ImageAssetModelBuilder implements AssetModelBuilder {

    public final File imageCache;

    public ImageAssetModelBuilder(@NonNull File imageCache) {
        this.imageCache = imageCache;
    }

    @Override
    public Model build() {
        final Texture texture = new Texture(Gdx.files.absolute(imageCache.getPath()));
        final Material material = new Material(TextureAttribute.createDiffuse(texture), new BlendingAttribute());

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
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal |
                VertexAttributes.Usage.TextureCoordinates);

        // TODO: make the value constant.
        final float coeff = 0.1f;
        final float width = texture.getWidth() * coeff;
        final float height = texture.getHeight() * coeff;
        model.meshes.get(0).scale(width, height, 1);

        return model;
    }
}
