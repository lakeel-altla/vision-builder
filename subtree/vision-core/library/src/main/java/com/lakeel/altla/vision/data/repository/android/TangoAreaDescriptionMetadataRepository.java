package com.lakeel.altla.vision.data.repository.android;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class TangoAreaDescriptionMetadataRepository {

    @NonNull
    public TangoAreaDescriptionMetaData get(@NonNull Tango tango, @NonNull String areaDescriptionId) {
        return tango.loadAreaDescriptionMetaData(areaDescriptionId);
    }

    @NonNull
    public List<TangoAreaDescriptionMetaData> findAll(@NonNull Tango tango) {
        List<String> areaDescriptionIds = tango.listAreaDescriptions();
        List<TangoAreaDescriptionMetaData> metaDatas = new ArrayList<>(areaDescriptionIds.size());

        for (String areaDescriptionId : areaDescriptionIds) {
            TangoAreaDescriptionMetaData metaData = get(tango, areaDescriptionId);
            metaDatas.add(metaData);
        }

        return metaDatas;
    }

    public void delete(@NonNull Tango tango, @NonNull String areaDescriptionId) {
        tango.deleteAreaDescription(areaDescriptionId);
    }
}
