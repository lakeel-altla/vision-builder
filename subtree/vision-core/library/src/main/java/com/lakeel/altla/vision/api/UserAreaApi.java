package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserAreaRepository;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.model.Area;

import android.support.annotation.NonNull;

public final class UserAreaApi extends BaseVisionApi {

    private final UserAreaRepository userAreaRepository;

    UserAreaApi(@NonNull VisionService visionService) {
        super(visionService);

        userAreaRepository = new UserAreaRepository(visionService.getFirebaseDatabase());
    }

    @NonNull
    public FirebaseQuery<Area> findByPlaceId(@NonNull String placeId) {
        return userAreaRepository.findByPlaceId(CurrentUser.getInstance().getUserId(), placeId);
    }

    public void save(@NonNull Area area) {
        if (!CurrentUser.getInstance().getUserId().equals(area.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userAreaRepository.save(area);
    }
}
