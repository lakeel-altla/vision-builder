package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.FirebaseDatabase;

import com.lakeel.altla.vision.model.Device;

import android.support.annotation.NonNull;

public final class UserDeviceRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userDevices";

    public UserDeviceRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull Device device) {
        if (device.getUserId() == null) throw new IllegalArgumentException("device.getUserId() must be not null.");

        device.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(device.getUserId())
                     .child(device.getId())
                     .setValue(device, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }
}
