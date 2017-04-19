package com.lakeel.altla.vision.builder.presentation.helper;

import com.lakeel.altla.vision.builder.presentation.event.SnackbarEvent;

import org.greenrobot.eventbus.EventBus;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public final class SnackbarEventHelper {

    private SnackbarEventHelper() {
    }

    public static void post(@NonNull EventBus eventBus, @StringRes int resId) {
        post(eventBus, new SnackbarEvent(resId));
    }

    public static void post(@NonNull EventBus eventBus, @NonNull SnackbarEvent event) {
        eventBus.post(event);
    }
}
