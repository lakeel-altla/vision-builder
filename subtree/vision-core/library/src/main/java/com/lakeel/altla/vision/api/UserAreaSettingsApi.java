package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserAreaSettingsRepository;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.AreaSettings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class UserAreaSettingsApi extends BaseVisionApi {

    private final UserAreaSettingsRepository userAreaSettingsRepository;

    public UserAreaSettingsApi(@NonNull VisionService visionService) {
        super(visionService);

        userAreaSettingsRepository = new UserAreaSettingsRepository(visionService.getFirebaseDatabase());
    }

    public void findUserAreaSettingsById(@NonNull String areaSettingsId,
                                         @Nullable OnSuccessListener<AreaSettings> onSuccessListener,
                                         @Nullable OnFailureListener onFailureListener) {
        userAreaSettingsRepository.find(CurrentUser.getInstance().getUserId(),
                                        areaSettingsId,
                                        onSuccessListener, onFailureListener);
    }

    public void findAllUserAreaSettings(@Nullable OnSuccessListener<List<AreaSettings>> onSuccessListener,
                                        @Nullable OnFailureListener onFailureListener) {
        userAreaSettingsRepository.findAll(CurrentUser.getInstance().getUserId(),
                                           onSuccessListener, onFailureListener);
    }

    public void saveUserAreaSettings(@NonNull AreaSettings areaSettings) {
        if (!CurrentUser.getInstance().getUserId().equals(areaSettings.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userAreaSettingsRepository.save(areaSettings);
    }
}
