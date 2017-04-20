package com.lakeel.altla.vision.data.repository.android;

import com.google.atap.tangoservice.Tango;

import android.support.annotation.NonNull;

import java.util.List;

public final class TangoAreaDescriptionIdRepository {

    @NonNull
    public List<String> findAll(@NonNull Tango tango) {
        return tango.listAreaDescriptions();
    }
}
