package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.vision.model.MeshComponent;

import android.support.annotation.NonNull;

public final class MeshComponentInstance extends ComponentInstance {

    @NonNull
    public final MeshComponent meshComponent;

    public MeshComponentInstance(@NonNull Model model, @NonNull ActorNode node) {
        super(model, node);

        this.meshComponent = node.actor.getRequiredComponent(MeshComponent.class);
    }
}
