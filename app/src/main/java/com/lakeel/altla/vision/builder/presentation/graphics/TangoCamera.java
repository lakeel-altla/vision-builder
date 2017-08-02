package com.lakeel.altla.vision.builder.presentation.graphics;

import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoPoseData;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;

import android.support.annotation.NonNull;

public final class TangoCamera extends PerspectiveCamera {

    public final Quaternion orientation = new Quaternion();

    public final Matrix4 invView = new Matrix4();

    public void resetTransform() {
        position.set(0, 0, 0);
        direction.set(0, 0, -1);
        up.set(0, 1, 0);
        orientation.idt();
    }

    public void setTangoCameraIntrinsics(@NonNull TangoCameraIntrinsics intrinsics) {
        fieldOfView = MathHelper.verticalFieldOfView(intrinsics);
        viewportWidth = intrinsics.width;
        viewportHeight = intrinsics.height;
    }

    public void setTangoPoseData(@NonNull TangoPoseData poseData) {
        if (poseData.statusCode == TangoPoseData.POSE_VALID) {
            position.set((float) poseData.translation[0],
                         (float) poseData.translation[1],
                         (float) poseData.translation[2]);
            orientation.set((float) poseData.rotation[0],
                            (float) poseData.rotation[1],
                            (float) poseData.rotation[2],
                            (float) poseData.rotation[3]);
            direction.set(0, 0, -1);
            up.set(0, 1, 0);
            rotate(orientation);
        }
    }

    @Override
    public void update(boolean updateFrustum) {
        super.update(updateFrustum);

        invView.set(view);
        invView.inv();
    }
}
