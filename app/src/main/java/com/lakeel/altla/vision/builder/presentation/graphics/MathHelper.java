package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tangoservice.TangoCameraIntrinsics;

import android.support.annotation.NonNull;

public final class MathHelper {

    private MathHelper() {
    }

    public static float verticalFieldOfView(@NonNull TangoCameraIntrinsics intrinsics) {
        return (float) Math.toDegrees(2f * Math.atan(0.5f * intrinsics.height / intrinsics.fy));
    }
}
