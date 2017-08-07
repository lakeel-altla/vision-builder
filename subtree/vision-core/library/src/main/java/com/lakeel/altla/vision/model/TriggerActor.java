package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;
import org.parceler.Transient;

import android.support.annotation.NonNull;

@Parcel(Parcel.Serialization.BEAN)
public final class TriggerActor extends Actor {

    private String shape = TriggerShape.UNKNOWN.name();

    public TriggerActor() {
        super(ActorType.TRIGGER);
    }

    @NonNull
    public String getShape() {
        return shape;
    }

    public void setShape(@NonNull String shape) {
        this.shape = shape;
    }

    @Exclude
    @Transient
    @NonNull
    public TriggerShape getShapeAsEnum() {
        return TriggerShape.valueOf(shape);
    }

    public void setShapeAsEnum(@NonNull TriggerShape shape) {
        this.shape = shape.name();
    }
}
