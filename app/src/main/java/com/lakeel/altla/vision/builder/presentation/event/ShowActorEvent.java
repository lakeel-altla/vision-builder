package com.lakeel.altla.vision.builder.presentation.event;

import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.Nullable;

public final class ShowActorEvent {

    @Nullable
    public final Scope scope;

    @Nullable
    public final String actorId;

    public ShowActorEvent(@Nullable Scope scope, @Nullable String actorId) {
        this.scope = scope;
        this.actorId = actorId;
    }
}
