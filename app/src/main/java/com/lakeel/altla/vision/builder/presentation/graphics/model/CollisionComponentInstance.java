package com.lakeel.altla.vision.builder.presentation.graphics.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.vision.model.CollisionComponent;

import android.support.annotation.NonNull;

public final class CollisionComponentInstance extends ComponentInstance {

    @NonNull
    public final CollisionComponent collisionComponent;

    public CollisionComponentInstance(@NonNull Model model, @NonNull ActorNode node,
                                      @NonNull CollisionComponent collisionComponent) {
        super(model, node);
        this.collisionComponent = collisionComponent;
    }
}
