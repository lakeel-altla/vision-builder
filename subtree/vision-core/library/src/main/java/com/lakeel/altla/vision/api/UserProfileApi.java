package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserProfileRepository;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Profile;

import android.support.annotation.NonNull;

public final class UserProfileApi extends BaseVisionApi {

    private UserProfileRepository userProfileRepository;

    public UserProfileApi(@NonNull VisionService visionService) {
        super(visionService);

        userProfileRepository = new UserProfileRepository(visionService.getFirebaseDatabase());
    }

    @NonNull
    public TypedQuery<Profile> findProfile(@NonNull String userId) {
        return userProfileRepository.find(userId);
    }
}
