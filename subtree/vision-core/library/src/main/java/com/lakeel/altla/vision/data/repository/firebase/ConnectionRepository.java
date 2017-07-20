package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.lakeel.altla.vision.helper.TypedQuery;

import android.support.annotation.NonNull;

public final class ConnectionRepository extends BaseDatabaseRepository {

    private static final String PATH = ".info/connected";

    public ConnectionRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    @NonNull
    public TypedQuery<Boolean> find() {
        final DatabaseReference reference = getDatabase().getReference(PATH);
        return new TypedQuery<>(reference, Boolean.class);
    }
}
