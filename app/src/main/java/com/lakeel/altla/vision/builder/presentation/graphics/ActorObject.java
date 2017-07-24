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
    private static final Vector3 TEMP_BASE_DIRECTION = new Vector3();

    // A temp vector.
    private static final Vector3 TEMP_DIRECTION = new Vector3();

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
        switch (axis) {
            case X:
                TEMP_BASE_DIRECTION.set(Vector3.X);
                break;
            case Y:
                TEMP_BASE_DIRECTION.set(Vector3.Y);
                break;
            case Z:
                TEMP_BASE_DIRECTION.set(Vector3.Z);
                break;
            default:
                throw new IllegalArgumentException("An unexpected 'axis' value: " + axis);
        }

        // Resolve the direction of a specified axis.
        transform.getRotation(TEMP_ROTATION);
        TEMP_DIRECTION.set(TEMP_BASE_DIRECTION).mul(TEMP_ROTATION);

        // Calculate the translaton along the specified axis.
        TEMP_TRANSLATION.set(TEMP_DIRECTION);
        TEMP_TRANSLATION.scl(distance);

        // Get the position of this actor.
        transform.getTranslation(TEMP_POSITION);

        // Calculate and set the new position of this actor.
        TEMP_POSITION.add(TEMP_TRANSLATION);
        transform.setTranslation(TEMP_POSITION);
    }

    public void fixTranslation() {
        transform.getTranslation(TEMP_POSITION);
        actor.setPositionX(TEMP_POSITION.x);
        actor.setPositionY(TEMP_POSITION.y);
        actor.setPositionZ(TEMP_POSITION.z);
    }
}
