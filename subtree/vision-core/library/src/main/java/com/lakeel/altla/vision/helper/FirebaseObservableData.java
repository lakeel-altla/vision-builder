package com.lakeel.altla.vision.helper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class FirebaseObservableData<TData> implements ObservableData<TData> {

    private static final Log LOG = LogFactory.getLog(FirebaseObservableData.class);

    private final Query query;

    private final DataSnapshotConverter<TData> dataSnapshotConverter;

    private final List<OnDataChangeListener<TData>> onDataChangeListeners = new ArrayList<>();

    private final List<OnFailureListener> onFailureListeners = new ArrayList<>();

    private ValueEventListener valueEventListener;

    private boolean closed;

    public FirebaseObservableData(@NonNull Query query, @NonNull DataSnapshotConverter<TData> dataSnapshotConverter) {
        this.query = query;
        this.dataSnapshotConverter = dataSnapshotConverter;
    }

    @Override
    public void addOnDataChangeListener(@NonNull OnDataChangeListener<TData> listener) {
        throwExceptionIfClosed();

        onDataChangeListeners.add(listener);
    }

    @Override
    public void removeOnDataChangeListener(@NonNull OnDataChangeListener<TData> listener) {
        onDataChangeListeners.remove(listener);
    }

    @Override
    public void addOnFailureListener(@NonNull OnFailureListener listener) {
        throwExceptionIfClosed();

        onFailureListeners.add(listener);
    }

    @Override
    public void removeOnFailureListener(@NonNull OnFailureListener listener) {
        onFailureListeners.remove(listener);
    }

    @Override
    public void observe() {
        throwExceptionIfClosed();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TData data = null;
                if (snapshot.exists()) {
                    data = dataSnapshotConverter.convert(snapshot);
                }
                if (data == null) {
                    LOG.w("The target data not found.");
                } else {
                    for (OnDataChangeListener<TData> onDataChangeListener : onDataChangeListeners) {
                        onDataChangeListener.onDataChange(data);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Exception e = error.toException();
                for (OnFailureListener onFailureListener : onFailureListeners) {
                    onFailureListener.onFailure(e);
                }
            }
        };
        query.addValueEventListener(valueEventListener);
    }

    @Override
    public void close() {
        throwExceptionIfClosed();

        if (valueEventListener != null) {
            query.removeEventListener(valueEventListener);
            valueEventListener = null;
        }

        closed = true;

        LOG.v("Closed.");
    }

    private void throwExceptionIfClosed() {
        if (closed) throw new IllegalStateException("This object is already closed.");
    }
}
