package com.lakeel.altla.vision.api;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;

import com.lakeel.altla.vision.data.repository.android.TangoAreaDescriptionIdRepository;
import com.lakeel.altla.vision.data.repository.android.TangoAreaDescriptionMetadataRepository;
import com.lakeel.altla.vision.data.repository.firebase.UserAreaDescriptionRepository;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.TangoAreaDescription;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TangoAreaDescriptionApi extends BaseVisionApi {

    private final TangoAreaDescriptionIdRepository tangoAreaDescriptionIdRepository;

    private final TangoAreaDescriptionMetadataRepository tangoAreaDescriptionMetadataRepository;

    private final UserAreaDescriptionRepository userAreaDescriptionRepository;

    public TangoAreaDescriptionApi(@NonNull VisionService visionService) {
        super(visionService);

        tangoAreaDescriptionIdRepository = new TangoAreaDescriptionIdRepository();
        tangoAreaDescriptionMetadataRepository = new TangoAreaDescriptionMetadataRepository();
        userAreaDescriptionRepository = new UserAreaDescriptionRepository(visionService.getFirebaseDatabase());
    }

    @Nullable
    public TangoAreaDescription findTangoAreaDescriptionById(@NonNull String areaDescriptionId) {
        Tango tango = getVisionService().getTangoWrapper().getTango();

        List<String> areaDescriptionIds = tangoAreaDescriptionIdRepository.findAll(tango);
        if (areaDescriptionIds.contains(areaDescriptionId)) {
            TangoAreaDescriptionMetaData metaData = tangoAreaDescriptionMetadataRepository.get(
                    tango, areaDescriptionId);
            return TangoAreaDescription.toTangoAreaDescription(metaData);
        } else {
            return null;
        }
    }

    @NonNull
    public List<TangoAreaDescription> findAllTangoAreaDescriptions() {
        Tango tango = getVisionService().getTangoWrapper().getTango();

        List<TangoAreaDescriptionMetaData> metaDatas = tangoAreaDescriptionMetadataRepository.findAll(tango);

        List<TangoAreaDescription> results = new ArrayList<>(metaDatas.size());
        for (TangoAreaDescriptionMetaData metaData : metaDatas) {
            results.add(TangoAreaDescription.toTangoAreaDescription(metaData));
        }

        return results;
    }

    public void deleteTangoAreaDescriptionById(@NonNull String areaDescriptionId) {
        Tango tango = getVisionService().getTangoWrapper().getTango();

        tangoAreaDescriptionMetadataRepository.delete(tango, areaDescriptionId);
    }

    public void exportTangoAreaDescriptionById(@NonNull String areaDescriptionId) {
        Tango tango = getVisionService().getTangoWrapper().getTango();

        TangoAreaDescription tangoAreaDescription = findTangoAreaDescriptionById(areaDescriptionId);
        if (tangoAreaDescription == null) {
            throw new IllegalArgumentException(
                    "TangoAreaDescription not found: areaDescriptionId = " + areaDescriptionId);
        }

        AreaDescription areaDescription = new AreaDescription();
        areaDescription.setId(tangoAreaDescription.getAreaDescriptionId());
        areaDescription.setUserId(CurrentUser.getInstance().getUserId());
        areaDescription.setName(tangoAreaDescription.getName());

        userAreaDescriptionRepository.save(areaDescription);
    }
}
