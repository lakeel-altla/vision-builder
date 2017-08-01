package com.lakeel.altla.vision.builder.presentation.graphics;

import com.badlogic.gdx.graphics.g3d.Model;
import com.lakeel.altla.vision.model.TriggerShape;

import android.support.annotation.NonNull;

public final class TriggerActorCursorObject extends ActorCursorObject {

    public final TriggerShape triggerShape;

    public TriggerActorCursorObject(@NonNull Model model, @NonNull TriggerShape triggerShape) {
        super(model);
        this.triggerShape = triggerShape;
    }
}
