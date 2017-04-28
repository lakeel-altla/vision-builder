package com.lakeel.altla.vision.model;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;
import org.parceler.Transient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Parcel(Parcel.Serialization.BEAN)
public final class Actor extends BaseEntity {

    private String areaId;

    private String assetType = AssetType.UNKNOWN.name();

    private String assetId;

    private String layer = Layer.UNKNOWN.name();

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

    @Nullable
    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(@Nullable String areaId) {
        this.areaId = areaId;
    }

    @NonNull
    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(@NonNull String assetType) {
        this.assetType = assetType;
    }

    @Exclude
    @Transient
    @NonNull
    public AssetType getAssetTypeAsEnum() {
        return AssetType.valueOf(assetType);
    }

    public void setAssetTypeAsEnum(@NonNull AssetType assetType) {
        this.assetType = assetType.name();
    }

    @Nullable
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(@Nullable String assetId) {
        this.assetId = assetId;
    }

    @NonNull
    public String getLayer() {
        return layer;
    }

    public void setLayer(@NonNull String layer) {
        this.layer = layer;
    }

    @Exclude
    @Transient
    @NonNull
    public Layer getLayerAsEnum() {
        return Layer.valueOf(layer);
    }

    public void setLayerAsEnum(@NonNull Layer layer) {
        this.layer = layer.name();
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
}
