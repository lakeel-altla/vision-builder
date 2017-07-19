package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class AreaDescriptionListByAreaModel {

    private final VisionService visionService;

    private final FirebaseQueryAdapter<AreaDescription> queryAdapter = new FirebaseQueryAdapter<>();

    private FirebaseQuery<AreaDescription> query;

    private int selectedPosition;

    public AreaDescriptionListByAreaModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @NonNull
    public FirebaseQueryAdapter<AreaDescription> getQueryAdapter() {
        return queryAdapter;
    }

    public void queryItems(@NonNull Scope scope, @NonNull String areaId) {
        selectedPosition = -1;
        queryAdapter.clear();

        switch (scope) {
            case PUBLIC:
                // TODO
                break;
            case USER:
                query = visionService.getUserAreaDescriptionApi().findByAreaId(areaId);
                break;
            default:
                throw new IllegalArgumentException("An unexpected scope: " + scope);
        }

        query.addListener(queryAdapter);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public AreaDescription getSelectedItem() {
        return (selectedPosition < 0) ? null : queryAdapter.getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }
}
