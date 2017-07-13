package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.lakeel.altla.vision.model.AssetType;

import android.support.annotation.NonNull;

public final class CursorObject extends ModelInstance {

    public final String assetId;

    public final AssetType assetType;

    public CursorObject(@NonNull Model model, @NonNull String assetId, @NonNull AssetType assetType) {
        super(model);
        this.assetId = assetId;
        this.assetType = assetType;
    }
}
