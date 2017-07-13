package com.lakeel.altla.vision.builder.presentation.view.pane;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

import butterknife.ButterKnife;

public abstract class Pane {

    @NonNull
    protected final Activity activity;

    @IdRes
    protected final int id;

    @NonNull
    protected final View view;

    protected Pane(@NonNull Activity activity, @IdRes int id) {
        this.activity = activity;
        this.id = id;
        this.view = activity.findViewById(id);
        ButterKnife.bind(this, view);
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void show() {
        view.setVisibility(View.VISIBLE);
        onShow();
    }

    public void hide() {
        view.setVisibility(View.GONE);
        onShow();
    }

    protected void onShow() {
    }

    protected void onHide() {
    }
}
