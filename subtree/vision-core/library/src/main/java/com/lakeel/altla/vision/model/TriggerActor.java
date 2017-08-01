package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;
import org.parceler.Transient;

import android.support.annotation.NonNull;

@Parcel(Parcel.Serialization.BEAN)
public final class TriggerActor extends Actor {

    private String triggerShape = TriggerShape.UNKNOWN.name();

    @NonNull
    public String getTriggerShape() {
        return triggerShape;
    }

    public void setTriggerShape(@NonNull String triggerShape) {
        this.triggerShape = triggerShape;
    }

    @Exclude
    @Transient
    @NonNull
    public TriggerShape getTriggerShapeAsEnum() {
        return TriggerShape.valueOf(triggerShape);
    }

    public void setTriggerShapeAsEnum(@NonNull TriggerShape triggerShape) {
        this.triggerShape = triggerShape.name();
    }
}
