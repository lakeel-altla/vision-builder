package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.model.BoxComponent;
import com.lakeel.altla.vision.model.ShapeComponent;
import com.lakeel.altla.vision.model.SphereComponent;
import com.lakeel.altla.vision.model.TriggerShape;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TriggerShapeListModel {

    private final List<Class<? extends ShapeComponent>> items = new ArrayList<>(TriggerShape.values().length - 1);

    private int selectedPosition;

    public TriggerShapeListModel() {
        items.add(BoxComponent.class);
        items.add(SphereComponent.class);
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public Class<? extends ShapeComponent> getItem(int position) {
        return items.get(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public Class<? extends ShapeComponent> getSelectedItem() {
        return (selectedPosition < 0) ? null : getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }
}
