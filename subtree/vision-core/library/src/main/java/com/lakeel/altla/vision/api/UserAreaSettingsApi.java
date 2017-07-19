package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserAreaSettingsRepository;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;

public final class UserAreaSettingsApi extends BaseVisionApi {

    private final UserAreaSettingsRepository userAreaSettingsRepository;

    public UserAreaSettingsApi(@NonNull VisionService visionService) {
        super(visionService);

        userAreaSettingsRepository = new UserAreaSettingsRepository(visionService.getFirebaseDatabase());
    }

    @NonNull
    public FirebaseQuery<AreaSettings> findAll() {
        return userAreaSettingsRepository.findAll(CurrentUser.getInstance().getUserId());
    }

    public void save(@NonNull AreaSettings areaSettings) {
        if (!CurrentUser.getInstance().getUserId().equals(areaSettings.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userAreaSettingsRepository.save(areaSettings);
    }
}
