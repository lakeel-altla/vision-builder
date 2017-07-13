package com.lakeel.altla.vision.builder.presentation.view.pane;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class PaneLifecycle {

    private final List<Pane> panes = new ArrayList<>();

    public void add(@NonNull Pane pane) {
        panes.add(pane);
    }

    public void onStart() {
        for (Pane pane : panes) {
            pane.onStart();
        }
    }

    public void onStop() {
        for (Pane pane : panes) {
            pane.onStop();
        }
    }

    public void onResume() {
        for (Pane pane : panes) {
            pane.onResume();
        }
    }

    public void onPause() {
        for (Pane pane : panes) {
            pane.onPause();
        }
    }
}
