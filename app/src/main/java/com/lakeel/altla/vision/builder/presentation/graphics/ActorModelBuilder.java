package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.vision.model.Actor;

public abstract class ActorModelBuilder {

    public final Actor actor;

    protected ActorModelBuilder(Actor actor) {
        this.actor = actor;
    }

    abstract Model build();
}
