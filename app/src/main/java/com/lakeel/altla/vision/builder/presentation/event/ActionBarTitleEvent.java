package com.lakeel.altla.vision.builder.presentation.event;

import android.support.annotation.Nullable;

public final class ActionBarTitleEvent {

    @Nullable
    public final String title;

    public ActionBarTitleEvent(@Nullable String title) {
        this.title = title;
    }
}
