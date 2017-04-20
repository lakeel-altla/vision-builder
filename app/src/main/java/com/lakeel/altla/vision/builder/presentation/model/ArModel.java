package com.lakeel.altla.vision.builder.presentation.model;

import com.lakeel.altla.android.property.ObjectProperty;
import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.model.Scope;

public final class ArModel {

    public final StringProperty areaSettingsId = new StringProperty();

    public final ObjectProperty<PickedActor> pickedActor = new ObjectProperty<>();

    public static final class PickedActor {

        public final Scope scope;

        public final String actorId;

        public PickedActor(Scope scope, String actorId) {
            this.scope = scope;
            this.actorId = actorId;
        }
    }
}
