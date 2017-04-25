package com.lakeel.altla.vision.builder.presentation.event;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public final class HomeAsUpIndicatorEvent {

    @Nullable
    public final Drawable indicator;

    public HomeAsUpIndicatorEvent(@Nullable Drawable indicator) {
        this.indicator = indicator;
    }
}
