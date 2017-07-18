package com.lakeel.altla.vision.helper;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class FirebaseQuery<TData> {

    private final Query query;

    private final DataSnapshotConverter<TData> converter;

    private final Map<ChildListener<TData>, ChildEventListener> childEventListenerMap = new HashMap<>();

    public FirebaseQuery(@NonNull Query query, @NonNull DataSnapshotConverter<TData> converter) {
        this.query = query;
        this.converter = converter;
    }

    public FirebaseQuery(@NonNull Query query, @NonNull Class<TData> clazz) {
        this(query, snapshot -> snapshot.getValue(clazz));
    }

    public void addListener(@NonNull ChildListener<TData> listener) {
        final ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                final TData data = converter.convert(snapshot);
                listener.onChildAdded(data, previousChildName);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                final TData data = converter.convert(snapshot);
                listener.onChildChanged(data, previousChildName);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                final TData data = converter.convert(snapshot);
                listener.onChildRemoved(data);
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                final TData data = converter.convert(snapshot);
                listener.onChildMoved(data, previousChildName);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                final Exception e = error.toException();
                listener.onError(e);
            }
        };
        childEventListenerMap.put(listener, childEventListener);
        query.addChildEventListener(childEventListener);
    }

    public void removeListener(@NonNull ChildListener<TData> listener) {
        final ChildEventListener childEventListener = childEventListenerMap.get(listener);
        if (childEventListener != null) {
            query.removeEventListener(childEventListener);
        }
    }

    public void clearListeners() {
        if (!childEventListenerMap.isEmpty()) {
            for (final ChildEventListener listener : childEventListenerMap.values()) {
                query.removeEventListener(listener);
            }
            childEventListenerMap.clear();
        }
    }

    public interface ChildListener<TData> {

        void onChildAdded(@NonNull TData data, @Nullable String previousChildName);

        void onChildChanged(@NonNull TData data, String previousChildName);

        void onChildRemoved(@NonNull TData data);

        void onChildMoved(@NonNull TData data, String previousChildName);

        void onError(@NonNull Exception e);
    }

    public static class BaseChildListener<TData> implements ChildListener<TData> {

        protected BaseChildListener() {
        }

        @Override
        public void onChildAdded(@NonNull TData data, @Nullable String previousChildName) {
        }

        @Override
        public void onChildChanged(@NonNull TData data, String previousChildName) {
        }

        @Override
        public void onChildRemoved(@NonNull TData data) {
        }

        @Override
        public void onChildMoved(@NonNull TData data, String previousChildName) {
        }

        @Override
        public void onError(@NonNull Exception e) {
        }
    }
}
