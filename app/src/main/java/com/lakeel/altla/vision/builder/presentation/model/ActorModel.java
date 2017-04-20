package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.rajawali.pool.Pool;
import com.lakeel.altla.rajawali.pool.QuaternionPool;
import com.lakeel.altla.vision.model.Actor;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import android.support.annotation.NonNull;

public abstract class ActorModel {

//    public final Scope scope;

    public final Actor actor;

    protected ActorModel(/*@NonNull Scope scope, */@NonNull Actor actor) {
//        this.scope = scope;
        this.actor = actor;
    }

    public void setPositionTo(@NonNull Vector3 position) {
        position.setAll(actor.getPositionX(),
                        actor.getPositionY(),
                        actor.getPositionZ());
    }

    public void setOrientationTo(@NonNull Quaternion quaternion) {
        quaternion.setAll(actor.getOrientationW(),
                          actor.getOrientationX(),
                          actor.getOrientationY(),
                          actor.getOrientationZ());
    }

    public void setScaleTo(@NonNull Vector3 scale) {
        scale.setAll(actor.getScaleX(),
                     actor.getScaleY(),
                     actor.getScaleZ());
    }

    public abstract Object3D create();

    public final void update(@NonNull Object3D object3D) {
        setPoseTo(object3D);

        updateOverride(object3D);
    }

    protected void updateOverride(@NonNull Object3D object3D) {
    }

    private void setPoseTo(@NonNull Object3D object3D) {
        setPositionTo(object3D.getPosition());

        try (Pool.Holder<Quaternion> holder = QuaternionPool.get()) {
            Quaternion orientation = holder.get();
            setOrientationTo(orientation);
            object3D.setOrientation(orientation);
        }

        setScaleTo(object3D.getScale());
    }

    public void setPosition(@NonNull Vector3 position) {
        actor.setPositionX(position.x);
        actor.setPositionY(position.y);
        actor.setPositionZ(position.z);
    }

    public void setOrientation(@NonNull Quaternion orientation) {
        actor.setOrientationX(orientation.x);
        actor.setOrientationY(orientation.y);
        actor.setOrientationZ(orientation.z);
        actor.setOrientationW(orientation.w);
    }

    public void setScale(@NonNull Vector3 scale) {
        actor.setScaleX(scale.x);
        actor.setScaleY(scale.y);
        actor.setScaleZ(scale.z);
    }
}
