package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import android.support.annotation.NonNull;

public final class ActorAxesInstance extends ModelInstance {

    private final ActorNode node;

    public ActorAxesInstance(@NonNull Model model, @NonNull ActorNode node) {
        super(model);
        this.node = node;
    }

    public void update() {
        transform.set(node.transform);
    }
}
