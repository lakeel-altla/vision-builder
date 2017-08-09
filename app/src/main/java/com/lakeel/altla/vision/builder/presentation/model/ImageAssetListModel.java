package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnProgressListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.model.Scope;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public final class ImageAssetListModel {

    private final VisionService visionService;

    private final FirebaseQueryAdapter<ImageAsset> queryAdapter = new FirebaseQueryAdapter<>();

    private TypedQuery<ImageAsset> query;

    private int selectedPosition;

    public ImageAssetListModel(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @NonNull
    public FirebaseQueryAdapter<ImageAsset> getQueryAdapter() {
        return queryAdapter;
    }

    public void queryItems(@NonNull Scope scope) {
        selectedPosition = -1;
        queryAdapter.clear();

        switch (scope) {
            case PUBLIC:
                // TODO
                break;
            case USER:
                query = visionService.getUserImageAssetApi().findAllImageAssets();
                break;
            default:
                throw new IllegalArgumentException("An unexpected scope: " + scope);
        }

        query.addTypedChildEventListener(queryAdapter);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Nullable
    public ImageAsset getSelectedItem() {
        return (selectedPosition < 0) ? null : queryAdapter.getItem(selectedPosition);
    }

    public boolean canSelect() {
        return 0 <= selectedPosition;
    }

    public void loadAssetFile(@NonNull String assetId,
                              @Nullable OnSuccessListener<File> onSuccessListener,
                              @Nullable OnFailureListener onFailureListener,
                              @Nullable OnProgressListener onProgressListener) {
        visionService.getUserAssetApi()
                     .loadAssetFile(assetId, onSuccessListener, onFailureListener, onProgressListener);
    }
}
