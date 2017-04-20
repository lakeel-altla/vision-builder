package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.model.Actor;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public final class ImageActorModel extends ActorModel {

    private static final Log LOG = LogFactory.getLog(ImageActorModel.class);

    private static final String TEXTURE_NAME = "texture";

    public Bitmap bitmap;

    public ImageActorModel(@NonNull Actor actor) {
        super(actor);
    }

    @Override
    public Object3D create() {
        // NOTE:
        // The argument 'textureName' is used as a variable in a fragment shader.
        Texture texture = new Texture(TEXTURE_NAME, R.drawable.ic_sync_black_24dp);

        Material material = new Material();
        material.setColorInfluence(0);
        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            LOG.e("Can not add a texture.", e);
        }

        Plane plane = new Plane(1, 1, 1, 1);
        plane.setMaterial(material);
        // Enable transparent images.
        plane.setTransparent(true);
        // Enable the back face rendering to understand how models rotate.
        plane.setDoubleSided(true);

        return plane;
    }

    @Override
    protected void updateOverride(@NonNull Object3D object3D) {
        super.updateOverride(object3D);

        if (bitmap != null) {
            Texture texture = new Texture(TEXTURE_NAME, bitmap);

            Material material = new Material();
            material.setColorInfluence(0);
            try {
                material.addTexture(texture);
            } catch (ATexture.TextureException e) {
                LOG.e("Can not add a texture.", e);
            }

            object3D.setMaterial(material);
        }
    }
}
