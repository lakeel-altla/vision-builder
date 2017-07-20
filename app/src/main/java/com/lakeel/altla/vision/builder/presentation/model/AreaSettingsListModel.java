package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class AreaSettingsListModel {

    private final VisionService visionService;

    private final FirebaseQueryAdapter<AreaSettings> queryAdapter = new FirebaseQueryAdapter<>();

    @Nullable
    private TypedQuery<AreaSettings> query;

    private int selectedPosition;

    public AreaSettingsListModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @NonNull
    public FirebaseQueryAdapter<AreaSettings> getQueryAdapter() {
        return queryAdapter;
    }

    public void queryItems() {
        selectedPosition = -1;
        queryAdapter.clear();

        query = visionService.getUserAreaSettingsApi().findAllAreaSettings();
        query.addTypedChildEventListener(queryAdapter);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public AreaSettings getSelectedItem() {
        return (selectedPosition < 0) ? null : queryAdapter.getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }
}
