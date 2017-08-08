package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.GeometryComponent;

import android.support.annotation.NonNull;

public final class GeometryObject extends ModelInstance {

    private static final float MAX_SCALE_RATIO = 1.2f;

    private static final float MIN_SCALE_RATIO = 0.8f;

    public final Actor actor;

    public final GeometryComponent geometryComponent;

    public final Vector3 position = new Vector3();

    public final Quaternion orientation = new Quaternion();

    public final Vector3 scale = new Vector3();

    public boolean transformDirty;

    // A temp vector.
    private final Vector3 tempAxisVector = new Vector3();

    // A temp vector.
    private final Quaternion tempOrientation = new Quaternion();

    public GeometryObject(@NonNull Model model, @NonNull Actor actor, @NonNull GeometryComponent geometryComponent) {
        super(model);

        this.actor = actor;
        this.geometryComponent = geometryComponent;

        position.set(geometryComponent.getPositionX(),
                     geometryComponent.getPositionY(),
                     geometryComponent.getPositionZ());

        orientation.set(geometryComponent.getOrientationX(),
                        geometryComponent.getOrientationY(),
                        geometryComponent.getOrientationZ(),
                        geometryComponent.getOrientationW());

        scale.set(geometryComponent.getScaleX(),
                  geometryComponent.getScaleY(),
                  geometryComponent.getScaleZ());

        transformDirty = true;
    }

    public void update() {
        if (transformDirty) {
            transformDirty = false;
            transform.set(position, orientation, scale);
        }
    }

    public void translate(@NonNull Axis axis, float distance) {
        axis.toVector3(tempAxisVector);

        // Calculate the direction of a specified axis.
        tempAxisVector.mul(orientation).nor();

        // Calculate a translaton along the specified axis.
        tempAxisVector.scl(distance);

        // Calculate and setTangoPoseData a new position of this actor.
        position.add(tempAxisVector);

        transformDirty = true;
    }

    public void rotate(@NonNull Axis axis, float degrees) {
        axis.toVector3(tempAxisVector);
        tempOrientation.setFromAxis(tempAxisVector, degrees);
        orientation.mul(tempOrientation);

        transformDirty = true;
    }

    public void scaleByExtent(float extent) {
        final float ratio = Math.min(Math.max(1 + extent, MIN_SCALE_RATIO), MAX_SCALE_RATIO);
        scale.scl(ratio);

        transformDirty = true;
    }

    public void savePositionToActor() {
        geometryComponent.setPosition(position.x, position.y, position.z);
    }

    public void saveOrientationToActor() {
        geometryComponent.setOrientation(orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public void saveScaleToActor() {
        geometryComponent.setScale(scale.x, scale.y, scale.z);
    }
}
