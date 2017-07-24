package com.lakeel.altla.vision.builder.presentation.model;

import com.badlogic.gdx.math.Vector3;

import android.support.annotation.NonNull;

/**
 * Defines x/y/z-axis.
 */
public enum Axis {

    X(0),
    Y(1),
    Z(2);

    private final int value;

    Axis(int value) {
        this.value = value;
    }

    /**
     * Gets the value assigned to this instance.
     *
     * @return The value assigned to this instance.
     */
    public int getValue() {
        return value;
    }

    @NonNull
    public Vector3 toVector3(@NonNull Vector3 result) {
        switch (this) {
            case X:
                return result.set(Vector3.X);
            case Y:
                return result.set(Vector3.Y);
            case Z:
                return result.set(Vector3.Z);
            default:
                return result.set(Vector3.Zero);
        }
    }
}
