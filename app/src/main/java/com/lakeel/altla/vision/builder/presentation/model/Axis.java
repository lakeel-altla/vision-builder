package com.lakeel.altla.vision.builder.presentation.model;

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
}
