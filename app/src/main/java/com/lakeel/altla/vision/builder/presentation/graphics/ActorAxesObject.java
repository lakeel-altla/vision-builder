package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import android.support.annotation.NonNull;

public final class ActorAxesObject extends ModelInstance {

    public ActorAxesObject(@NonNull Model model) {
        super(model);
    }

    public void update(@NonNull MeshActorObject meshActorObject) {
        transform.set(meshActorObject.position, meshActorObject.orientation, meshActorObject.scale);
    }
}
