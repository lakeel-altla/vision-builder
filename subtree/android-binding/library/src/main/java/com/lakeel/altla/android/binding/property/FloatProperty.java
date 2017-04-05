package com.lakeel.altla.android.binding.property;

public class FloatProperty extends AbstractFloatProperty {

    private float value;

    public FloatProperty() {
    }

    public FloatProperty(float value) {
        this.value = value;
    }

    @Override
    public final float get() {
        return value;
    }

    @Override
    public final void set(float value) {
        if (this.value != value) {
            float oldValue = this.value;
            this.value = value;
            onValueChanged(oldValue, this.value);
        }
    }

    protected void onValueChanged(float oldValue, float newValue) {
        raiseOnValueChanged();
    }
}
