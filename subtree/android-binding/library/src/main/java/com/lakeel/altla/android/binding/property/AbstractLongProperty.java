package com.lakeel.altla.android.binding.property;

import android.support.annotation.Nullable;

public abstract class AbstractLongProperty extends BaseProperty {

    protected AbstractLongProperty() {
    }

    public abstract long get();

    public abstract void set(long value);

    @Nullable
    @Override
    public final Object getAsObject() {
        return get();
    }

    @Override
    public final void setAsObject(@Nullable Object value) {
        set(value == null ? 0 : (long) value);
    }
}
