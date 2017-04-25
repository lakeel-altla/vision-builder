package com.lakeel.altla.vision.builder.presentation.event;

import android.support.annotation.NonNull;

public final class HomeAsUpVisibleEvent {

    @NonNull
    public static final HomeAsUpVisibleEvent VISIBLE = new HomeAsUpVisibleEvent(true);

    @NonNull
    public static final HomeAsUpVisibleEvent INVISIBLE = new HomeAsUpVisibleEvent(false);

    public final boolean visible;

    private HomeAsUpVisibleEvent(boolean visible) {
        this.visible = visible;
    }
}
