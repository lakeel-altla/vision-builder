package com.lakeel.altla.vision.api;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;

import android.support.annotation.NonNull;

public class BaseVisionApi {

    private final Log log = LogFactory.getLog(getClass());

    private final VisionService visionService;

    protected BaseVisionApi(@NonNull VisionService visionService) {
        this.visionService = visionService;
    }

    @NonNull
    protected VisionService getVisionService() {
        return visionService;
    }

    @NonNull
    protected final Log getLog() {
        return log;
    }
}
