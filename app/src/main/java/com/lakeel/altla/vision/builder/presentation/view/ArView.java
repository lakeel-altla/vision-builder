package com.lakeel.altla.vision.builder.presentation.view;

import com.google.atap.tango.ux.TangoUx;

import com.lakeel.altla.vision.builder.presentation.model.Axis;

import org.rajawali3d.renderer.ISurfaceRenderer;

import android.support.annotation.StringRes;

/**
 * Defines the main view.
 */
public interface ArView {

    void setTangoUxLayout(TangoUx tangoUx);

    void setSurfaceRenderer(ISurfaceRenderer renderer);

    void onResumeTextureView();

    void onPauseTextureView();

    void onUpdateMainMenuVisible(boolean visible);

    void onUpdateImageButtonAssetListVisible(boolean visible);

    void onUpdateAreaSettingsVisible(boolean visible);

    void onUpdateActorContinerViewVisible(boolean visible);

    void onUpdateAssetListVisible(boolean visible);

    void onUpdateObjectMenuVisible(boolean visible);

    void onUpdateTranslateSelected(boolean selected);

    void onUpdateTranslateMenuVisible(boolean visible);

    void onUpdateTranslateAxisSelected(Axis axis, boolean selected);

    void onUpdateRotateSelected(boolean selected);

    void onUpdateRotateMenuVisible(boolean visible);

    void onUpdateRotateAxisSelected(Axis axis, boolean selected);

    void onUpdateScaleSelected(boolean selected);

    void onShowSignInView();

    void onSnackbar(@StringRes int resId);
}
