package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.FirebaseDatabase;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.support.annotation.NonNull;

public class BaseDatabaseRepository {

    private final Log log = LogFactory.getLog(getClass());

    private final FirebaseDatabase database;

    protected BaseDatabaseRepository(@NonNull FirebaseDatabase database) {
        this.database = database;
    }

    @NonNull
    protected final FirebaseDatabase getDatabase() {
        return database;
    }

    @NonNull
    protected final Log getLog() {
        return log;
    }
}
