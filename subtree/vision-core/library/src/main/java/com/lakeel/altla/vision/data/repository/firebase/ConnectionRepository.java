package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.lakeel.altla.vision.helper.FirebaseObservableData;
import com.lakeel.altla.vision.helper.ObservableData;

import android.support.annotation.NonNull;

public final class ConnectionRepository extends BaseDatabaseRepository {

    private static final String PATH = ".info/connected";

    public ConnectionRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    @NonNull
    public ObservableData<Boolean> observe() {
        DatabaseReference reference = getDatabase().getReference(PATH);
        return new FirebaseObservableData<>(reference, snapshot -> snapshot.getValue(Boolean.class));
    }
}
