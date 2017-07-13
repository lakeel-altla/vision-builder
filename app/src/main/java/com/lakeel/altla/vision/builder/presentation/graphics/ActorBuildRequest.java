package com.lakeel.altla.vision.builder.presentation.graphics;

import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class ActorBuildRequest {

    public final Actor actor;

    public final AssetModelBuilder builder;

    public ActorBuildRequest(@NonNull Actor actor, @NonNull AssetModelBuilder builder) {
        this.actor = actor;
        this.builder = builder;
    }
}
