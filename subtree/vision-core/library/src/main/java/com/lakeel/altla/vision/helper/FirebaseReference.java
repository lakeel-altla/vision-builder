package com.lakeel.altla.vision.helper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class FirebaseReference<TData> {

    private final DatabaseReference reference;

    private final DataSnapshotConverter<TData> converter;

    private final Map<ValueListener<TData>, ValueEventListener> valueEventListenerMap = new HashMap<>();

    public FirebaseReference(@NonNull DatabaseReference reference, @NonNull DataSnapshotConverter<TData> converter) {
        this.reference = reference;
        this.converter = converter;
    }

    public FirebaseReference(@NonNull DatabaseReference reference, @NonNull Class<TData> clazz) {
        this(reference, snapshot -> snapshot.getValue(clazz));
    }

    public void addListener(@NonNull ValueListener<TData> listener) {
        final ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final TData data = converter.convert(snapshot);
                listener.onDataChange(data);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                final Exception e = error.toException();
                listener.onError(e);
            }
        };
        valueEventListenerMap.put(listener, valueEventListener);
        reference.addValueEventListener(valueEventListener);
    }

    public void removeListener(@NonNull ValueListener<TData> listener) {
        final ValueEventListener valueEventListener = valueEventListenerMap.get(listener);
        if (valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
        }
    }

    public void clearListeners() {
        if (!valueEventListenerMap.isEmpty()) {
            for (final ValueEventListener listener : valueEventListenerMap.values()) {
                reference.removeEventListener(listener);
            }
            valueEventListenerMap.clear();
        }
    }

    public interface ValueListener<TData> {

        void onDataChange(@Nullable TData data);

        void onError(@NonNull Exception e);
    }

    public static class BaseValueListener<TData> implements ValueListener<TData> {

        protected BaseValueListener() {
        }

        @Override
        public void onDataChange(@Nullable TData data) {
        }

        @Override
        public void onError(@NonNull Exception e) {
        }
    }
}
