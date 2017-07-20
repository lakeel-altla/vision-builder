package com.lakeel.altla.vision.helper;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class TypedQuery<TData> {

    private final Query query;

    private final DataSnapshotConverter<TData> converter;

    @Nullable
    private Map<TypedValueEventListener<TData>, ValueEventListener> valueEventListenerMap;

    @Nullable
    private Map<TypedChildEventListener<TData>, ChildEventListener> childEventListenerMap;

    public TypedQuery(@NonNull Query query, @NonNull DataSnapshotConverter<TData> converter) {
        this.query = query;
        this.converter = converter;
    }

    public TypedQuery(@NonNull Query query, @NonNull Class<TData> clazz) {
        this(query, snapshot -> snapshot.getValue(clazz));
    }

    public void addTypedValueEventListener(@NonNull TypedValueEventListener<TData> listener) {
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

        if (valueEventListenerMap == null) {
            valueEventListenerMap = new HashMap<>();
        }
        valueEventListenerMap.put(listener, valueEventListener);

        query.addValueEventListener(valueEventListener);
    }

    public void addListenerForSingleValue(@NonNull TypedValueEventListener<TData> listener) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
        });
    }

    public void removeTypedValueEventListener(@NonNull TypedValueEventListener<TData> listener) {
        if (valueEventListenerMap != null) {
            final ValueEventListener valueEventListener = valueEventListenerMap.get(listener);
            if (valueEventListener != null) {
                query.removeEventListener(valueEventListener);
            }
        }
    }

    public void addTypedChildEventListener(@NonNull TypedChildEventListener<TData> listener) {
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

        if (childEventListenerMap == null) {
            childEventListenerMap = new HashMap<>();
        }
        childEventListenerMap.put(listener, childEventListener);

        query.addChildEventListener(childEventListener);
    }

    public void removeTypedChildEventListener(@NonNull TypedChildEventListener<TData> listener) {
        if (childEventListenerMap != null) {
            final ChildEventListener childEventListener = childEventListenerMap.get(listener);
            if (childEventListener != null) {
                query.removeEventListener(childEventListener);
            }
        }
    }

    public interface TypedValueEventListener<TData> {

        void onDataChange(@Nullable TData data);

        void onError(@NonNull Exception e);
    }

    public interface TypedChildEventListener<TData> {

        void onChildAdded(@NonNull TData data, @Nullable String previousChildName);

        void onChildChanged(@NonNull TData data, String previousChildName);

        void onChildRemoved(@NonNull TData data);

        void onChildMoved(@NonNull TData data, String previousChildName);

        void onError(@NonNull Exception e);
    }
}
