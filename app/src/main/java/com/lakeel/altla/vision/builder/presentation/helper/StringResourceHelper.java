package com.lakeel.altla.vision.builder.presentation.helper;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

public final class StringResourceHelper {

    private StringResourceHelper() {
    }

    @StringRes
    public static int resolveScopeStringResource(@Nullable Scope scope) {
        int resId = R.string.label_area_mode_public;
        if (scope != null && scope == Scope.USER) {
            resId = R.string.label_area_mode_user;
        }
        return resId;
    }
}
