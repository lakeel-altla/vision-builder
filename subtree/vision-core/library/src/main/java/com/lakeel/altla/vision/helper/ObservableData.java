package com.lakeel.altla.vision.helper;

import android.support.annotation.NonNull;

import java.io.Closeable;

public interface ObservableData<TData> extends Closeable {

    void addOnDataChangeListener(@NonNull OnDataChangeListener<TData> listener);

    void removeOnDataChangeListener(@NonNull OnDataChangeListener<TData> listener);

    void addOnFailureListener(@NonNull OnFailureListener listener);

    void removeOnFailureListener(@NonNull OnFailureListener listener);

    void observe();
}
