package com.lakeel.altla.android.binding.property;

import android.support.annotation.Nullable;

import java.util.Objects;

public class ObjectProperty<T> extends AbstractObjectProperty<T> {

    private T value;

    public ObjectProperty() {
    }

    public ObjectProperty(T value) {
        this.value = value;
    }

    @Nullable
    @Override
    public final T get() {
        return value;
    }

    @Override
    public final void set(@Nullable T value) {
        if (!Objects.equals(this.value, value)) {
            T oldValue = this.value;
            this.value = value;
            onValueChanged(oldValue, this.value);
        }
    }

    protected void onValueChanged(@Nullable T oldValue, @Nullable T newValue) {
        raiseOnValueChanged();
    }
}
