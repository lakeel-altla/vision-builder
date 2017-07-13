package com.lakeel.altla.vision.builder.presentation.view.pane;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

public final class PaneGroup {

    private final SparseArrayCompat<Pane> paneMap = new SparseArrayCompat<>();

    private Pane activePane;

    public void add(@NonNull Pane pane) {
        paneMap.put(pane.id, pane);
        pane.hide();
    }

    public void show(@IdRes int id) {
        final Pane pane = paneMap.get(id);
        if (pane == null) throw new IllegalArgumentException("'id' is invalid.");

        if (activePane != null) {
            activePane.hide();
            activePane = null;
        }

        activePane = pane;
        activePane.show();
    }
}
