package com.lakeel.altla.vision.helper;

import android.support.annotation.NonNull;

import java.io.Closeable;

public interface ObservableList<TData> extends Closeable {

    void addOnDataListEventListener(@NonNull OnObservableListEventListener<TData> listener);

    void removeOnDataListEventListener(@NonNull OnObservableListEventListener<TData> listener);

    void addOnFailureListener(@NonNull OnFailureListener listener);

    void removeOnFailureListener(@NonNull OnFailureListener listener);

    void observe();
}
