package com.lakeel.altla.vision.domain.usecase;

import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.domain.repository.TangoAreaDescriptionMetadataRepository;

import javax.inject.Inject;

import rx.Single;
import rx.schedulers.Schedulers;

public final class DeleteTangoAreaDescriptionUseCase {

    @Inject
    TangoAreaDescriptionMetadataRepository tangoAreaDescriptionMetadataRepository;

    @Inject
    public DeleteTangoAreaDescriptionUseCase() {
    }

    public Single<String> execute(String areaDescriptionId) {
        if (areaDescriptionId == null) throw new ArgumentNullException("areaDescriptionId");

        return tangoAreaDescriptionMetadataRepository.delete(areaDescriptionId)
                                                     .subscribeOn(Schedulers.io());
    }
}
