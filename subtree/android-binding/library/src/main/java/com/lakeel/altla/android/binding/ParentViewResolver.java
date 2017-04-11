package com.lakeel.altla.android.binding;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public final class ParentViewResolver implements ViewResolver {

    private final View view;

    public ParentViewResolver(@NonNull View view) {
        this.view = view;
    }

    @Nullable
    @Override
    public View findViewById(@IdRes int id) {
        return view.findViewById(id);
    }
}
