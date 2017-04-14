package com.lakeel.altla.vision.builder.presentation.event;

import android.support.annotation.NonNull;

public final class CloseViewEvent {

    @NonNull
    public final Class<?> viewType;

    public CloseViewEvent(@NonNull Class<?> viewType) {
        this.viewType = viewType;
    }
}
