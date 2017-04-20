package com.lakeel.altla.vision.api;

import com.google.firebase.iid.FirebaseInstanceId;

import com.lakeel.altla.vision.data.repository.firebase.UserDeviceConnectionRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.DeviceConnection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class UserDeviceConnectionApi extends BaseVisionApi {

    private final UserDeviceConnectionRepository userDeviceConnectionRepository;

    public UserDeviceConnectionApi(@NonNull VisionService visionService) {
        super(visionService);

        userDeviceConnectionRepository = new UserDeviceConnectionRepository(visionService.getFirebaseDatabase());
    }

    public void markUserDeviceConnectionAsOnline(@Nullable OnSuccessListener<Void> onSuccessListener,
                                                 @Nullable OnFailureListener onFailureListener) {
        String userId = CurrentUser.getInstance().getUserId();
        String instanceId = FirebaseInstanceId.getInstance().getId();

        userDeviceConnectionRepository.find(userId, instanceId, connection -> {
            if (connection == null) {
                connection = new DeviceConnection();
                connection.setId(instanceId);
                connection.setUserId(userId);
            }
            connection.setOnline(true);

            userDeviceConnectionRepository.save(connection);
            userDeviceConnectionRepository.markAsOfflineWhenDisconnected(userId, instanceId);

            if (onSuccessListener != null) onSuccessListener.onSuccess(null);
        }, onFailureListener);
    }

    public void markUserDeviceConnectionAsOffline(@Nullable OnSuccessListener<Void> onSuccessListener,
                                                  @Nullable OnFailureListener onFailureListener) {
        String userId = CurrentUser.getInstance().getUserId();
        String instanceId = FirebaseInstanceId.getInstance().getId();

        userDeviceConnectionRepository.find(userId, instanceId, connection -> {
            if (connection == null) {
                connection = new DeviceConnection();
                connection.setId(instanceId);
                connection.setUserId(userId);
            }
            connection.setOnline(false);
            connection.setLastOnlineAtAsLong(-1);

            userDeviceConnectionRepository.save(connection);

            if (onSuccessListener != null) onSuccessListener.onSuccess(null);
        }, onFailureListener);
    }
}
