package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class ActorObject extends ModelInstance {

    private static final float MAX_SCALE_RATIO = 1.2f;

    private static final float MIN_SCALE_RATIO = 0.8f;

    // A temp vector.
    private static final Vector3 TEMP_AXIS_VECTOR = new Vector3();

    // A temp vector.
    private static final Quaternion TEMP_ORIENTATION = new Quaternion();

    public final Actor actor;

    public final Vector3 position = new Vector3();

    public final Quaternion orientation = new Quaternion();

    public final Vector3 scale = new Vector3();

    public boolean transformDirty;

    public ActorObject(@NonNull Model model, @NonNull Actor actor) {
        super(model);
        this.actor = actor;

        position.set((float) actor.getPositionX(), (float) actor.getPositionY(), (float) actor.getPositionZ());
        orientation.set((float) actor.getOrientationX(),
                        (float) actor.getOrientationY(),
                        (float) actor.getOrientationZ(),
                        (float) actor.getOrientationW());
        scale.set((float) actor.getScaleX(), (float) actor.getScaleY(), (float) actor.getScaleZ());

        transformDirty = true;
    }

    public void update() {
        if (transformDirty) {
            transformDirty = false;
            transform.set(position, orientation, scale);
        }
    }

    public void translate(@NonNull Axis axis, float distance) {
        axis.toVector3(TEMP_AXIS_VECTOR);

        // Calculate the direction of a specified axis.
        TEMP_AXIS_VECTOR.mul(orientation).nor();

        // Calculate a translaton along the specified axis.
        TEMP_AXIS_VECTOR.scl(distance);

        // Calculate and set a new position of this actor.
        position.add(TEMP_AXIS_VECTOR);

        transformDirty = true;
    }

    public void stopTranslate() {
        actor.setPosition(position.x, position.y, position.z);
    }

    public void rotate(@NonNull Axis axis, float degrees) {
        axis.toVector3(TEMP_AXIS_VECTOR);
        TEMP_ORIENTATION.setFromAxis(TEMP_AXIS_VECTOR, degrees);
        orientation.mul(TEMP_ORIENTATION);

        transformDirty = true;
    }

    public void stopRotate() {
        actor.setOrientation(orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public void scale(float delta) {
        final float ratio = Math.min(Math.max(1 + delta, MIN_SCALE_RATIO), MAX_SCALE_RATIO);
        scale.scl(ratio);

        transformDirty = true;
    }

    public void stopScale() {
        actor.setScale(scale.x, scale.y, scale.z);
    }
}
