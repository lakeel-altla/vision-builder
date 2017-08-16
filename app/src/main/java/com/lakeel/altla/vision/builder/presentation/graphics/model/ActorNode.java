package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.TransformComponent;

import android.support.annotation.NonNull;

public final class ActorNode {

    private static final Log LOG = LogFactory.getLog(ActorNode.class);

    private static final float MAX_SCALE_RATIO = 1.2f;

    private static final float MIN_SCALE_RATIO = 0.8f;

    @NonNull
    public final Actor actor;

    @NonNull
    public final Vector3 position = new Vector3();

    @NonNull
    public final Quaternion orientation = new Quaternion();

    @NonNull
    public final Vector3 scale = new Vector3();

    public boolean transformDirty;

    @NonNull
    public final Matrix4 transform = new Matrix4();

    private final Array<ComponentInstance> componentInstances = new Array<>();

    private ComponentInstance mainComponentInstance;

    // A temp vector.
    private final Vector3 tempAxisVector = new Vector3();

    // A temp vector.
    private final Quaternion tempOrientation = new Quaternion();

    public ActorNode(@NonNull Actor actor) {
        this.actor = actor;

        final TransformComponent transformComponent = actor.getRequiredTransformComponent();

        position.set(transformComponent.getPositionX(),
                     transformComponent.getPositionY(),
                     transformComponent.getPositionZ());

        orientation.set(transformComponent.getOrientationX(),
                        transformComponent.getOrientationY(),
                        transformComponent.getOrientationZ(),
                        transformComponent.getOrientationW());

        scale.set(transformComponent.getScaleX(),
                  transformComponent.getScaleY(),
                  transformComponent.getScaleZ());

        transformDirty = true;
    }

    public int getComponentInstanceCount() {
        return componentInstances.size;
    }

    @NonNull
    public ComponentInstance getComponentInstance(int index) {
        return componentInstances.get(index);
    }

    void addComponentInstance(@NonNull ComponentInstance componentInstance) {
        componentInstances.add(componentInstance);
        // Must be initialize the transform of a component instance by this node's one.
        componentInstance.transform.set(transform);
    }

    public void update() {
        final boolean transformDirtyMemento = transformDirty;

        if (transformDirty) {
            transformDirty = false;
            transform.set(position, orientation, scale);
        }

        for (int i = 0; i < componentInstances.size; i++) {
            componentInstances.get(i).update(transformDirtyMemento);
        }
    }

    public void collectVisibleInstances(@NonNull Array<ModelInstance> out) {
        // TODO
        out.addAll(componentInstances);
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
        actor.getRequiredTransformComponent()
             .setPosition(position.x, position.y, position.z);
    }

    public void saveOrientationToActor() {
        actor.getRequiredTransformComponent()
             .setOrientation(orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public void saveScaleToActor() {
        actor.getRequiredTransformComponent()
             .setScale(scale.x, scale.y, scale.z);
    }
}
