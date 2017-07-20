package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserAreaRepository;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Area;

import android.support.annotation.NonNull;

public final class UserAreaApi extends BaseVisionApi {

    private final UserAreaRepository userAreaRepository;

    UserAreaApi(@NonNull VisionService visionService) {
        super(visionService);

        userAreaRepository = new UserAreaRepository(visionService.getFirebaseDatabase());
    }

    @NonNull
    public TypedQuery<Area> findAreaByPlaceId(@NonNull String placeId) {
        return userAreaRepository.findByPlaceId(CurrentUser.getInstance().getUserId(), placeId);
    }

    public void saveArea(@NonNull Area area) {
        if (!CurrentUser.getInstance().getUserId().equals(area.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userAreaRepository.save(area);
    }
}
