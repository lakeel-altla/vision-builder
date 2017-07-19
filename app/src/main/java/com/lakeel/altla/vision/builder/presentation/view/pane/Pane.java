package com.lakeel.altla.vision.builder.presentation.view.pane;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.ButterKnife;

public abstract class Pane {

    @NonNull
    protected final Activity activity;

    @IdRes
    protected final int id;

    @NonNull
    protected final View view;

    @Nullable
    private OnVisibleChangedListener onVisibleChangedListener;

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
        if (onVisibleChangedListener != null) onVisibleChangedListener.onVisibleChanged(true);
    }

    public void hide() {
        view.setVisibility(View.GONE);
        onShow();
        if (onVisibleChangedListener != null) onVisibleChangedListener.onVisibleChanged(false);
    }

    @Nullable
    public OnVisibleChangedListener getOnVisibleChangedListener() {
        return onVisibleChangedListener;
    }

    public void setOnVisibleChangedListener(@Nullable OnVisibleChangedListener onVisibleChangedListener) {
        this.onVisibleChangedListener = onVisibleChangedListener;
    }

    protected void onShow() {
    }

    protected void onHide() {
    }

    public interface OnVisibleChangedListener {

        void onVisibleChanged(boolean visible);
    }
}
