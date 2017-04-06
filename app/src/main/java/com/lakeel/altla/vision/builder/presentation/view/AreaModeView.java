package com.lakeel.altla.vision.builder.presentation.view;

import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;

public interface AreaModeView {

    void onAreaModeSelected(@NonNull Scope scope);

    void onCloseView();
}
