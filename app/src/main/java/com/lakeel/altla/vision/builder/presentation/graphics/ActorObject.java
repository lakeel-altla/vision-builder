package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class ActorObject extends ModelInstance {

    private static final float MAX_SCALING_RATIO = 1.2f;

    private static final float MIN_SCALING_RATIO = 0.8f;

    // A temp vector.
    private static final Vector3 TEMP_AXIS_VECTOR = new Vector3();

    // A temp vector.
    private static final Quaternion TEMP_ROTATION = new Quaternion();

    // A temp vector.
    private static final Vector3 TEMP_TRANSLATION = new Vector3();

    // A temp vector.
    private static final Vector3 TEMP_SCALE = new Vector3();

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

    public void translate(@NonNull Axis axis, float distance) {
        axis.toVector3(TEMP_AXIS_VECTOR);

        // Resolve the direction of a specified axis.
        transform.getRotation(TEMP_ROTATION);
        TEMP_AXIS_VECTOR.mul(TEMP_ROTATION).nor();

        // Calculate the translaton along the specified axis.
        TEMP_AXIS_VECTOR.scl(distance);

        // Get the position of this actor.
        transform.getTranslation(TEMP_TRANSLATION);

        // Calculate and set the new position of this actor.
        TEMP_TRANSLATION.add(TEMP_AXIS_VECTOR);
        transform.setTranslation(TEMP_TRANSLATION);
    }

    public void fixTranslation() {
        transform.getTranslation(TEMP_TRANSLATION);
        actor.setPosition(TEMP_TRANSLATION.x,
                          TEMP_TRANSLATION.y,
                          TEMP_TRANSLATION.z);
    }

    public void rotate(@NonNull Axis axis, float degrees) {
        axis.toVector3(TEMP_AXIS_VECTOR);
        transform.rotate(TEMP_AXIS_VECTOR, degrees);
    }

    public void fixRotation() {
        transform.getRotation(TEMP_ROTATION);
        actor.setOrientation(TEMP_ROTATION.x,
                             TEMP_ROTATION.y,
                             TEMP_ROTATION.z,
                             TEMP_ROTATION.w);
    }

    public void scale(float delta) {
        final float ratio = Math.min(Math.max(1 + delta, MIN_SCALING_RATIO), MAX_SCALING_RATIO);
        transform.scale(ratio, ratio, ratio);
    }

    public void fixScaling() {
        transform.getScale(TEMP_SCALE);
        actor.setScale(TEMP_SCALE.x,
                       TEMP_SCALE.y,
                       TEMP_SCALE.z);
    }
}
