package com.lakeel.altla.vision.builder.presentation.event;

import android.support.annotation.NonNull;

public final class BackViewEvent {

    @NonNull
    public final Object view;

    public BackViewEvent(@NonNull Object view) {
        this.view = view;
    }
}
