package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.vision.model.Asset;

import android.support.annotation.NonNull;

public final class MeshActorCursorObject extends ActorCursorObject {

    public final Asset asset;

    public MeshActorCursorObject(@NonNull Model model, @NonNull Asset asset) {
        super(model);
        this.asset = asset;
    }
}
