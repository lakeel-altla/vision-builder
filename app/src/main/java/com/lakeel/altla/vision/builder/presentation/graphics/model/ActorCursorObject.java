package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.builder.presentation.graphics.TangoCamera;

import android.support.annotation.NonNull;

public class ActorCursorObject extends ModelInstance {

    public final Vector3 position = new Vector3();

    public final Quaternion orientation = new Quaternion();

    public ActorCursorObject(@NonNull Model model) {
        super(model);
    }

    public final void update(@NonNull TangoCamera camera) {
        position.set(0, 0, -1);
        position.mul(camera.invView);

        // Use the camera rotation as an own one.
        orientation.set(camera.orientation);
        transform.idt();
        transform.rotate(orientation);

        transform.setTranslation(position);
    }
}
