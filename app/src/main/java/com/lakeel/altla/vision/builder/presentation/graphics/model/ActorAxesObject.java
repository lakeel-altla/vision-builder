package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import android.support.annotation.NonNull;

public final class ActorAxesObject extends ModelInstance {

    public ActorAxesObject(@NonNull Model model) {
        super(model);
    }

    public void update(@NonNull GeometryObject geometryObject) {
        transform.set(geometryObject.position, geometryObject.orientation, geometryObject.scale);
    }
}
