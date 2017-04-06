package com.lakeel.altla.vision.builder.presentation.view;

import android.support.annotation.StringRes;

public interface ActorView {

    void onUpdateMainMenuVisible(boolean visible);

    void onCloseView();

    void onSnackbar(@StringRes int resId);
}
