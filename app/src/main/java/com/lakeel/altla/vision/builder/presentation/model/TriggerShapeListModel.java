package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.model.TriggerShape;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TriggerShapeListModel {

    private final List<TriggerShape> items = new ArrayList<>(TriggerShape.values().length - 1);

    private int selectedPosition;

    public TriggerShapeListModel() {
        for (final TriggerShape triggerShape : TriggerShape.values()) {
            if (triggerShape != TriggerShape.UNKNOWN) {
                items.add(triggerShape);
            }
        }
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public TriggerShape getItem(int position) {
        return items.get(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public TriggerShape getSelectedItem() {
        return (selectedPosition < 0) ? null : getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }
}
