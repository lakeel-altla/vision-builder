package com.lakeel.altla.vision.api;

import com.lakeel.altla.vision.data.repository.firebase.UserAreaRepository;
import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.ObservableList;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Area;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class UserAreaApi extends BaseVisionApi {

    private final UserAreaRepository userAreaRepository;

    UserAreaApi(@NonNull VisionService visionService) {
        super(visionService);

        userAreaRepository = new UserAreaRepository(visionService.getFirebaseDatabase());
    }

    public void findAreaById(@NonNull String areaId,
                             @Nullable OnSuccessListener<Area> onSuccessListener,
                             @Nullable OnFailureListener onFailureListener) {
        userAreaRepository.find(CurrentUser.getInstance().getUserId(), areaId, onSuccessListener, onFailureListener);
    }

    public void findAreasByPlaceId(@NonNull String placeId,
                                   @Nullable OnSuccessListener<List<Area>> onSuccessListener,
                                   @Nullable OnFailureListener onFailureListener) {
        userAreaRepository.findByPlaceId(CurrentUser.getInstance().getUserId(), placeId,
                                         onSuccessListener, onFailureListener);
    }

    public void findAllAreas(@Nullable OnSuccessListener<List<Area>> onSuccessListener,
                             @Nullable OnFailureListener onFailureListener) {
        userAreaRepository.findAll(CurrentUser.getInstance().getUserId(), onSuccessListener, onFailureListener);
    }

    @NonNull
    public ObservableData<Area> observeAreaById(@NonNull String areaId) {
        return userAreaRepository.observe(CurrentUser.getInstance().getUserId(), areaId);
    }

    @NonNull
    public ObservableList<Area> observeAreas() {
        return userAreaRepository.observeAll(CurrentUser.getInstance().getUserId());
    }

    public void saveArea(@NonNull Area area) {
        if (!CurrentUser.getInstance().getUserId().equals(area.getUserId())) {
            throw new IllegalArgumentException("Invalid user id.");
        }

        userAreaRepository.save(area);
    }
}
