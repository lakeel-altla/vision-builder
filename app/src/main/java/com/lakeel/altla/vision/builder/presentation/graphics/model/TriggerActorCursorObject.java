package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.vision.model.ShapeComponent;

import android.support.annotation.NonNull;

public final class TriggerActorCursorObject extends ActorCursorObject {

    public final Class<? extends ShapeComponent> shapeClass;

    public TriggerActorCursorObject(@NonNull Model model, Class<? extends ShapeComponent> shapeClass) {
        super(model);
        this.shapeClass = shapeClass;
    }
}
