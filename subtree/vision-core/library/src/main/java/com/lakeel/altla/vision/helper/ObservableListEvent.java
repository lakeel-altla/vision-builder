package com.lakeel.altla.vision.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class ObservableListEvent<TData> {

    private final Type type;

    private final TData data;

    private final String previousChildName;

    ObservableListEvent(@NonNull Type type, @NonNull TData data, @Nullable String previousChildName) {
        this.type = type;
        this.data = data;
        this.previousChildName = previousChildName;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public TData getData() {
        return data;
    }

    @Nullable
    public String getPreviousChildName() {
        return previousChildName;
    }

    public enum Type {
        ADDED,
        MOVED,
        REMOVED,
        CHANGED
    }
}
