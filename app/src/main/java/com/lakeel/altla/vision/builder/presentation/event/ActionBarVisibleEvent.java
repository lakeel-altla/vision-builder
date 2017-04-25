package com.lakeel.altla.vision.builder.presentation.event;

import android.support.annotation.NonNull;

public final class ActionBarVisibleEvent {

    @NonNull
    public static final ActionBarVisibleEvent VISIBLE = new ActionBarVisibleEvent(true);

    @NonNull
    public static final ActionBarVisibleEvent INVISIBLE = new ActionBarVisibleEvent(false);

    public final boolean visible;

    private ActionBarVisibleEvent(boolean visible) {
        this.visible = visible;
    }
}
