package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class AreaListByPlaceModel {

    private final VisionService visionService;

    private final FirebaseQueryAdapter<Area> queryAdapter = new FirebaseQueryAdapter<>();

    private FirebaseQuery<Area> query;

    private int selectedPosition;

    public AreaListByPlaceModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @NonNull
    public FirebaseQueryAdapter<Area> getQueryAdapter() {
        return queryAdapter;
    }

    public void queryItems(@NonNull Scope scope, @NonNull String placeId) {
        selectedPosition = -1;
        queryAdapter.clear();

        switch (scope) {
            case PUBLIC:
                // TODO
                break;
            case USER:
                query = visionService.getUserAreaApi().findByPlaceId(placeId);
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
    public Area getSelectedItem() {
        return (selectedPosition < 0) ? null : queryAdapter.getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }
}
