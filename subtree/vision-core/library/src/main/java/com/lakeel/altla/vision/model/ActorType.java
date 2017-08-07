package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public enum ActorType {
    UNKNOWN(Actor.class),
    MESH(MeshActor.class),
    TRIGGER(TriggerActor.class);

    public final Class<? extends Actor> actorClass;

    ActorType(@NonNull Class<? extends Actor> actorClass) {
        this.actorClass = actorClass;
    }
}
