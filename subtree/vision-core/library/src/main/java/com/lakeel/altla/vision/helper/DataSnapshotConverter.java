package com.lakeel.altla.vision.helper;

import com.google.firebase.database.DataSnapshot;

import android.support.annotation.NonNull;

public interface DataSnapshotConverter<TData> {

    TData convert(@NonNull DataSnapshot snapshot);
}
