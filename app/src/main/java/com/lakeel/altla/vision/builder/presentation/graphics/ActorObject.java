package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class ActorObject extends ModelInstance {

    // A temp vector.
    private static final Vector3 TEMP_AXIS_VECTOR = new Vector3();

    // A temp vector.
    private static final Vector3 TEMP_TRANSLATION = new Vector3();

    // A temp vector.
    private static final Quaternion TEMP_ROTATION = new Quaternion();

    // A temp vector.
    private static final Vector3 TEMP_POSITION = new Vector3();

    public final Actor actor;

    public ActorObject(@NonNull Model model, @NonNull Actor actor) {
        super(model);
        this.actor = actor;

        transform.translate((float) actor.getPositionX(),
                            (float) actor.getPositionY(),
                            (float) actor.getPositionZ());

        Quaternion rotation = new Quaternion((float) actor.getOrientationX(),
                                             (float) actor.getOrientationY(),
                                             (float) actor.getOrientationZ(),
                                             (float) actor.getOrientationW());
        transform.rotate(rotation);

        transform.scale((float) actor.getScaleX() * transform.getScaleX(),
                        (float) actor.getScaleY() * transform.getScaleY(),
                        (float) actor.getScaleZ() * transform.getScaleZ());
    }

    public void translateAlong(@NonNull Axis axis, float distance) {
        axis.toVector3(TEMP_AXIS_VECTOR);

        // Resolve the direction of a specified axis.
        transform.getRotation(TEMP_ROTATION);
        TEMP_AXIS_VECTOR.mul(TEMP_ROTATION).nor();

        // Calculate the translaton along the specified axis.
        TEMP_TRANSLATION.set(TEMP_AXIS_VECTOR);
        TEMP_TRANSLATION.scl(distance);

        // Get the position of this actor.
        transform.getTranslation(TEMP_POSITION);

        // Calculate and set the new position of this actor.
        TEMP_POSITION.add(TEMP_TRANSLATION);
        transform.setTranslation(TEMP_POSITION);
    }

    public void fixTranslation() {
        transform.getTranslation(TEMP_POSITION);
        actor.setPosition(TEMP_POSITION.x,
                          TEMP_POSITION.y,
                          TEMP_POSITION.z);
    }

    public void rotateAround(@NonNull Axis axis, float degrees) {
        axis.toVector3(TEMP_AXIS_VECTOR);

        // Rotate.
        transform.rotate(TEMP_AXIS_VECTOR, degrees);
    }

    public void fixRotation() {
        transform.getRotation(TEMP_ROTATION);
        actor.setOrientation(TEMP_ROTATION.x,
                             TEMP_ROTATION.y,
                             TEMP_ROTATION.z,
                             TEMP_ROTATION.w);
    }
}
