package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class SphereCollisionComponent extends CollisionComponent {

    public static final String TYPE = "SphereCollision";

    private float centerX;

    private float centerY;

    private float centerZ;

    private float radius;

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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setCenter(float x, float y, float z) {
        centerX = x;
        centerY = y;
        centerZ = z;
    }
}
