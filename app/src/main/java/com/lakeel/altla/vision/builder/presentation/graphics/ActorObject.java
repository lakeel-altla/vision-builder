package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class ActorObject extends ModelInstance {

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
}
