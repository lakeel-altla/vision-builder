package com.lakeel.altla.android.binding;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;

public interface ViewContainer {

    @Nullable
    View findViewById(@IdRes int id);
}
