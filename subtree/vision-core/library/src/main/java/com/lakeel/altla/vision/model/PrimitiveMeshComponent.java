package com.lakeel.altla.vision.model;

public abstract class PrimitiveMeshComponent extends MeshComponent {

    private float colorR;

    private float colorG;

    private float colorB;

    private float colorA;

    public float getColorR() {
        return colorR;
    }

    public void setColorR(float colorR) {
        this.colorR = colorR;
    }

    public float getColorG() {
        return colorG;
    }

    public void setColorG(float colorG) {
        this.colorG = colorG;
    }

    public float getColorB() {
        return colorB;
    }

    public void setColorB(float colorB) {
        this.colorB = colorB;
    }

    public float getColorA() {
        return colorA;
    }

    public void setColorA(float colorA) {
        this.colorA = colorA;
    }
}
