package com.lakeel.altla.vision.data.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.DeviceConnection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class UserDeviceConnectionRepository extends BaseDatabaseRepository {

    private static final String BASE_PATH = "userDeviceConnections";

    private static final String PATH_ONLINE = "online";

    private static final String PATH_LAST_ONLINE_AT = "lastOnlineAt";

    private static final String PATH_UPDATED_AT = "updatedAt";

    public UserDeviceConnectionRepository(@NonNull FirebaseDatabase database) {
        super(database);
    }

    public void save(@NonNull DeviceConnection connection) {
        if (connection.getUserId() == null) {
            throw new IllegalArgumentException("connection.getUserId() must be not null.");
        }

        connection.setUpdatedAtAsLong(-1);

        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(connection.getUserId())
                     .child(connection.getId())
                     .setValue(connection, (error, reference) -> {
                         if (error != null) {
                             getLog().e(String.format("Failed to save: reference = %s", reference),
                                        error.toException());
                         }
                     });
    }

    public void find(@NonNull String userId, @NonNull String instanceId,
                     @Nullable OnSuccessListener<DeviceConnection> onSuccessListener,
                     @Nullable OnFailureListener onFailureListener) {
        getDatabase().getReference()
                     .child(BASE_PATH)
                     .child(userId)
                     .child(instanceId)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             DeviceConnection connection = snapshot.getValue(DeviceConnection.class);
                             if (onSuccessListener != null) onSuccessListener.onSuccess(connection);
                         }

                         @Override
                         public void onCancelled(DatabaseError error) {
                             if (onFailureListener != null) onFailureListener.onFailure(error.toException());
                         }
                     });
    }

    public void markAsOfflineWhenDisconnected(@NonNull String userId, @NonNull String instanceId) {
        DatabaseReference connectionReference = getDatabase().getReference()
                                                             .child(BASE_PATH)
                                                             .child(userId)
                                                             .child(instanceId);
        DatabaseReference onlineReference = connectionReference.child(PATH_ONLINE);
        DatabaseReference lastOnlineTimeReference = connectionReference.child(PATH_LAST_ONLINE_AT);
        DatabaseReference updatedAtReference = connectionReference.child(PATH_UPDATED_AT);

        onlineReference.onDisconnect().setValue(Boolean.FALSE, (error, reference) -> {
            if (error != null) {
                getLog().e(String.format("Failed to do setValue: reference = %s", reference), error.toException());
            }
        });

        lastOnlineTimeReference.onDisconnect().setValue(ServerValue.TIMESTAMP, (error, reference) -> {
            if (error != null) {
                getLog().e(String.format("Failed to do setValue: reference = %s", reference), error.toException());
            }
        });

        updatedAtReference.onDisconnect().setValue(ServerValue.TIMESTAMP, (error, reference) -> {
            if (error != null) {
                getLog().e(String.format("Failed to do setValue: reference = %s", reference), error.toException());
            }
        });
    }
}
