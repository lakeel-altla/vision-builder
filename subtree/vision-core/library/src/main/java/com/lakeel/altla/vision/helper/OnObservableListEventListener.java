package com.lakeel.altla.vision.helper;

import android.support.annotation.NonNull;

public interface OnObservableListEventListener<TData> {

    void onObservableListEvent(@NonNull ObservableListEvent<TData> event);
}
