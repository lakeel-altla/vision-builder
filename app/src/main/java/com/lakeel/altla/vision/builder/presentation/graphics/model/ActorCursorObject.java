package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.builder.presentation.graphics.TangoCamera;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.TransformComponent;

import android.support.annotation.NonNull;

public final class ActorCursorObject extends ModelInstance {

    @NonNull
    public final Vector3 position = new Vector3();

    @NonNull
    public final Quaternion orientation = new Quaternion();

    @NonNull
    public final Actor actor;

    private final TangoCamera camera;

    public ActorCursorObject(@NonNull Model model, @NonNull Actor actor, @NonNull TangoCamera camera) {
        super(model);

        if (actor.getTransformComponent() == null) {
            throw new IllegalArgumentException("'actor.transformComponent' is null.");
        }

        this.actor = actor;
        this.camera = camera;
    }

    public void update() {
        position.set(0, 0, -1);
        position.mul(camera.invView);

        // Use the camera rotation as an own one.
        orientation.set(camera.orientation);
        transform.idt();
        transform.rotate(orientation);

        transform.setTranslation(position);
    }

    public void saveTransform() {
        final TransformComponent transformComponent = actor.getRequiredTransformComponent();
        transformComponent.setPosition(position.x, position.y, position.z);
        transformComponent.setOrientation(orientation.x, orientation.y, orientation.z, orientation.w);
        transformComponent.setScale(1, 1, 1);
    }
}
