package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.ConnectionRepository;
import com.lakeel.altla.vision.helper.ObservableData;

import android.support.annotation.NonNull;

public final class FirebaseConnectionApi extends BaseVisionApi {

    private final ConnectionRepository connectionRepository;

    public FirebaseConnectionApi(@NonNull VisionService visionService) {
        super(visionService);

        connectionRepository = new ConnectionRepository(visionService.getFirebaseDatabase());
    }

    @NonNull
    public ObservableData<Boolean> observeConnection() {
        return connectionRepository.observe();
    }
}
