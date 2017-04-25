package com.lakeel.altla.vision.builder.presentation.event;

import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;

public final class AreaModeSelectedEvent {

    @NonNull
    public final Scope scope;

    public AreaModeSelectedEvent(@NonNull Scope scope) {
        this.scope = scope;
    }
}
