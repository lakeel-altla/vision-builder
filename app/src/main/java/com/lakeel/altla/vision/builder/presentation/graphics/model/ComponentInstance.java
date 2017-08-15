package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import android.support.annotation.NonNull;

public class ComponentInstance extends ModelInstance {

    @NonNull
    public final ActorNode node;

    public ComponentInstance(@NonNull Model model, @NonNull ActorNode node) {
        super(model);
        this.node = node;
    }

    public void update(boolean transformDirty) {
        if (transformDirty) {
            transform.set(node.transform);
        }
    }
}
