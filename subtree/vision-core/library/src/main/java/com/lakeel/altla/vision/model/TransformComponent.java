package com.lakeel.altla.vision.model;

import android.support.annotation.NonNull;

public final class TransformComponent extends Component {

    public static final String TYPE = "Transform";

    private float positionX;

    private float positionY;

    private float positionZ;

    private float orientationX;

    private float orientationY;

    private float orientationZ;

    private float orientationW;

    private float scaleX = 1;

    private float scaleY = 1;

    private float scaleZ = 1;

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public float getPositionZ() {
        return positionZ;
    }

    public void setPositionZ(float positionZ) {
        this.positionZ = positionZ;
    }

    public float getOrientationX() {
        return orientationX;
    }

    public void setOrientationX(float orientationX) {
        this.orientationX = orientationX;
    }

    public float getOrientationY() {
        return orientationY;
    }

    public void setOrientationY(float orientationY) {
        this.orientationY = orientationY;
    }

    public float getOrientationZ() {
        return orientationZ;
    }

    public void setOrientationZ(float orientationZ) {
        this.orientationZ = orientationZ;
    }

    public float getOrientationW() {
        return orientationW;
    }

    public void setOrientationW(float orientationW) {
        this.orientationW = orientationW;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(float scaleZ) {
        this.scaleZ = scaleZ;
    }

    public void setPosition(float x, float y, float z) {
        positionX = x;
        positionY = y;
        positionZ = z;
    }

    public void setOrientation(float x, float y, float z, float w) {
        orientationX = x;
        orientationY = y;
        orientationZ = z;
        orientationW = w;
    }

    public void setScale(float x, float y, float z) {
        scaleX = x;
        scaleY = y;
        scaleZ = z;
    }
}
