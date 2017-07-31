package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.support.annotation.NonNull;

import java.io.File;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.TextureCoordinates;

public final class ImageMeshActorModelBuilder implements MeshActorModelBuilder {

    private static final Log LOG = LogFactory.getLog(ImageMeshActorModelBuilder.class);

    // The texture scalling factor: 512 pixels = 1 meter.
    private static final float SCALING_FACTOR = 1f / 512f;

    public final File imageCache;

    public ImageMeshActorModelBuilder(@NonNull File imageCache) {
        this.imageCache = imageCache;
    }

    @Override
    public Model build() {
        LOG.d("Loading the texture: path = %s", imageCache.getPath());

        final Texture texture = new Texture(Gdx.files.absolute(imageCache.getPath()));

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

        return model;
    }
}
