package com.lakeel.altla.vision.builder.presentation.helper;

import com.google.atap.tango.mesh.TangoMesh;
import com.google.atap.tango.reconstruction.Tango3dReconstruction;
import com.google.atap.tango.reconstruction.Tango3dReconstructionConfig;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class TangoMesher {

    private static final Log LOG = LogFactory.getLog(TangoMesher.class);

    private final TangoPointCloudManager tangoPointCloudManager = new TangoPointCloudManager();

    private final Tango3dReconstruction tango3dReconstruction;

    private final HandlerThread handlerThread = new HandlerThread("mesherCallback");

    private final Handler handler;

    private boolean reconstructionActive;

    private Runnable runnable;

    /**
     * Callback for when meshes are available.
     */
    public interface OnTangoMeshesAvailableListener {

        void onTangoMeshesAvailable(@NonNull List<TangoMesh> tangoMeshes);
    }

    public TangoMesher(@NonNull final OnTangoMeshesAvailableListener listener) {
        final Tango3dReconstructionConfig config = new Tango3dReconstructionConfig();
        config.putBoolean("generate_color", false);
        tango3dReconstruction = new Tango3dReconstruction(config);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        runnable = () -> {
            synchronized (TangoMesher.this) {
                if (!reconstructionActive) {
                    return;
                }

                final TangoPointCloudData pointCloudData = tangoPointCloudManager.getLatestPointCloud();
                if (pointCloudData == null) {
                    return;
                }

                final TangoPoseData poseData = TangoSupport.getPoseAtTime(
                        pointCloudData.timestamp,
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH,
                        TangoSupport.TANGO_SUPPORT_ENGINE_TANGO,
                        TangoSupport.TANGO_SUPPORT_ENGINE_TANGO,
                        TangoSupport.ROTATION_IGNORED);
                if (poseData.statusCode != TangoPoseData.POSE_VALID) {
                    return;
                }

                final List<int[]> gridIndices = tango3dReconstruction.update(pointCloudData, poseData, null, null);
                final int count = gridIndices.size();
                final List<TangoMesh> tangoMeshes = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    final int[] gridIndex = gridIndices.get(i);
                    final TangoMesh tangoMesh = tango3dReconstruction.extractMeshSegment(gridIndex);
                    if (0 < tangoMesh.numVertices && 0 < tangoMesh.numFaces) {
                        tangoMeshes.add(tangoMesh);
                    }
                }

                listener.onTangoMeshesAvailable(tangoMeshes);
            }
        };
    }

    /**
     * Synchronize access to mTango3dReconstruction. This runs in UI thread.
     */
    public synchronized void release() {
        reconstructionActive = false;
        tango3dReconstruction.release();
    }

    public void startSceneReconstruction() {
        reconstructionActive = true;
    }

    public void stopSceneReconstruction() {
        reconstructionActive = false;
    }

    /**
     * Synchronize access to mTango3dReconstruction. This runs in UI thread.
     */
    public synchronized void resetSceneReconstruction() {
        tango3dReconstruction.clear();
    }

    /**
     * Synchronize access to mTango3dReconstruction. This runs in UI thread.
     */
    public synchronized void setColorCameraCalibration(TangoCameraIntrinsics calibration) {
        tango3dReconstruction.setColorCameraCalibration(calibration);
    }

    /**
     * Synchronize access to mTango3dReconstruction. This runs in UI thread.
     */
    public synchronized void setDepthCameraCalibration(TangoCameraIntrinsics calibration) {
        tango3dReconstruction.setDepthCameraCalibration(calibration);
    }

    /**
     * Receives the depth point cloud. This method retrieves and stores the depth camera pose
     * and point cloud to later use when updating the {@code Tango3dReconstruction}.
     *
     * @param pointCloudData the depth point cloud.
     */
    public void onPointCloudAvailable(final TangoPointCloudData pointCloudData) {
        if (!reconstructionActive || pointCloudData == null || pointCloudData.points == null) {
            return;
        }

        tangoPointCloudManager.updatePointCloud(pointCloudData);
        handler.removeCallbacksAndMessages(null);
        handler.post(runnable);
    }
}
