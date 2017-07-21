package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import android.support.annotation.NonNull;

public final class ActorAxesObject extends ModelInstance {

    private final Vector3 translation = new Vector3();

    private final Quaternion rotation = new Quaternion();

    public ActorAxesObject(@NonNull Model model) {
        super(model);
    }

    public void transform(@NonNull ActorObject actorObject) {
        transform.idt();

        actorObject.transform.getTranslation(translation);
        actorObject.transform.getRotation(rotation);

        // Rotate and then translate.
        transform.rotate(rotation);
        transform.setTranslation(translation);
    }
}
