package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.storage.FirebaseStorage;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.support.annotation.NonNull;

public class BaseStorageRepository {

    private final Log log = LogFactory.getLog(getClass());

    private final FirebaseStorage storage;

    protected BaseStorageRepository(@NonNull FirebaseStorage storage) {
        this.storage = storage;
    }

    @NonNull
    protected final FirebaseStorage getStorage() {
        return storage;
    }

    @NonNull
    protected final Log getLog() {
        return log;
    }
}
