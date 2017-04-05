package com.lakeel.altla.android.binding;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Property {

    @Nullable
    Object getAsObject();

    void setAsObject(@Nullable Object value);

    void addOnValueChangedListener(@NonNull OnValueChangedListener listener);

    void removeOnValueChangedListener(@NonNull OnValueChangedListener listener);

    interface OnValueChangedListener {

        void onValueChanged(@NonNull Property sender);
    }
}
