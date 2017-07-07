package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.lakeel.altla.vision.model.Actor;

import android.support.annotation.NonNull;

public final class ArObject extends ModelInstance {

    final Actor actor;

    public ArObject(@NonNull Model model, @NonNull Actor actor) {
        super(model);
        this.actor = actor;
    }
}
