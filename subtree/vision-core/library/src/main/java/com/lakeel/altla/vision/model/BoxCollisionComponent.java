package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class BoxCollisionComponent extends CollisionComponent {

    public static final String TYPE = "BoxCollision";

    private float centerX;

    private float centerY;

    private float centerZ;

    private float sizeX;

    private float sizeY;

    private float sizeZ;

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public float getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(float centerZ) {
        this.centerZ = centerZ;
    }

    public float getSizeX() {
        return sizeX;
    }

    public void setSizeX(float sizeX) {
        this.sizeX = sizeX;
    }

    public float getSizeY() {
        return sizeY;
    }

    public void setSizeY(float sizeY) {
        this.sizeY = sizeY;
    }

    public float getSizeZ() {
        return sizeZ;
    }

    public void setSizeZ(float sizeZ) {
        this.sizeZ = sizeZ;
    }

    public void setCenter(float x, float y, float z) {
        centerX = x;
        centerY = y;
        centerZ = z;
    }

    public void setSize(float x, float y, float z) {
        sizeX = x;
        sizeY = y;
        sizeZ = z;
    }
}
