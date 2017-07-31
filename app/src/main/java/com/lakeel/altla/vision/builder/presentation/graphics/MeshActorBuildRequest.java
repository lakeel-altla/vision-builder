package com.lakeel.altla.vision.builder.presentation.graphics;

import com.lakeel.altla.vision.model.MeshActor;

import android.support.annotation.NonNull;

public final class MeshActorBuildRequest {

    public final MeshActor actor;

    public final MeshActorModelBuilder builder;

    public MeshActorBuildRequest(@NonNull MeshActor actor, @NonNull MeshActorModelBuilder builder) {
        this.actor = actor;
        this.builder = builder;
    }
}
