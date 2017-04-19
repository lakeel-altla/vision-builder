package com.lakeel.altla.vision.builder.presentation.event;

import android.support.annotation.StringRes;

public final class SnackbarEvent {

    @StringRes
    public final int resource;

    public SnackbarEvent(@StringRes int resource) {
        this.resource = resource;
    }
}
