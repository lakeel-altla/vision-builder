package com.lakeel.altla.vision.model;

import org.parceler.Parcel;

import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public class Actor extends BaseEntity {

    private String name;

    private double positionX;

    private double positionY;

    private double positionZ;

    private double orientationX;

    private double orientationY;

    private double orientationZ;

    private double orientationW;

    private double scaleX = 1;

    private double scaleY = 1;

    private double scaleZ = 1;

    protected Actor() {
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public double getPositionZ() {
        return positionZ;
    }

    public void setPositionZ(double positionZ) {
        this.positionZ = positionZ;
    }

    public void setPosition(double x, double y, double z) {
        positionX = x;
        positionY = y;
        positionZ = z;
    }

    public double getOrientationX() {
        return orientationX;
    }

    public void setOrientationX(double orientationX) {
        this.orientationX = orientationX;
    }

    public double getOrientationY() {
        return orientationY;
    }

    public void setOrientationY(double orientationY) {
        this.orientationY = orientationY;
    }

    public double getOrientationZ() {
        return orientationZ;
    }

    public void setOrientationZ(double orientationZ) {
        this.orientationZ = orientationZ;
    }

    public double getOrientationW() {
        return orientationW;
    }

    public void setOrientationW(double orientationW) {
        this.orientationW = orientationW;
    }

    public void setOrientation(double x, double y, double z, double w) {
        orientationX = x;
        orientationY = y;
        orientationZ = z;
        orientationW = w;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public double getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(double scaleZ) {
        this.scaleZ = scaleZ;
    }

    public void setScale(double x, double y, double z) {
        scaleX = x;
        scaleY = y;
        scaleZ = z;
    }
}
