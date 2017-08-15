package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.model.BoxMeshComponent;
import com.lakeel.altla.vision.model.PrimitiveMeshComponent;
import com.lakeel.altla.vision.model.SphereMeshComponent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TriggerShapeListModel {

    private final List<Class<? extends PrimitiveMeshComponent>> items = new ArrayList<>();

    private int selectedPosition;

    public TriggerShapeListModel() {
        items.add(BoxMeshComponent.class);
        items.add(SphereMeshComponent.class);
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public Class<? extends PrimitiveMeshComponent> getItem(int position) {
        return items.get(position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public Class<? extends PrimitiveMeshComponent> getSelectedItem() {
        return (selectedPosition < 0) ? null : getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }
}
