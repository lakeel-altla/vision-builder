package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.atap.tangoservice.TangoPoseData;

import com.lakeel.altla.tango.OnPoseAvailableListener;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import android.os.Handler;
import android.os.Looper;

import javax.inject.Inject;

public final class ConsolePresenter extends BasePresenter<ConsolePresenter.View>
        implements OnPoseAvailableListener {

    private static final int SECS_TO_MILLISECS = 1000;

    private static final double UPDATE_DEBUG_CONSOLE_INTERVAL = 100;

    @Inject
    VisionService visionService;

    private final Handler handlerMain = new Handler(Looper.getMainLooper());

    // on non-UI thread.
    private LocalizationState localizationState = LocalizationState.UNKNOWN;

    private PoseDebugger debuggerAd2Ss = new PoseDebugger(debugger -> {
        // on UI-thread.
        getView().onUpdateAd2SsTranslation(debugger.translation.x,
                                           debugger.translation.y,
                                           debugger.translation.z);
    });

    private PoseDebugger debuggerAd2D = new PoseDebugger(debugger -> {
        // on UI-thread.
        getView().onUpdateAd2DTranslation(debugger.translation.x,
                                          debugger.translation.y,
                                          debugger.translation.z);
    });

    private PoseDebugger debuggerSs2D = new PoseDebugger(debugger -> {
        // on UI-thread.
        getView().onUpdateSs2DTranslation(debugger.translation.x,
                                          debugger.translation.y,
                                          debugger.translation.z);
    });

    @Inject
    public ConsolePresenter() {
    }

    @Override
    protected void onResumeOverride() {
        super.onResumeOverride();

        visionService.getTangoWrapper().addOnPoseAvailableListener(this);
    }

    @Override
    protected void onPauseOverride() {
        super.onPauseOverride();

        visionService.getTangoWrapper().removeOnPoseAvailableListener(this);
    }

    @Override
    public void onPoseAvailable(TangoPoseData pose) {
        // NOTE: Invoked by Tango's thread that is not UI one.

        if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION &&
            pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE) {

            debuggerAd2Ss.debug(pose);

            if (pose.statusCode == TangoPoseData.POSE_VALID) {
                if (localizationState == LocalizationState.UNKNOWN ||
                    localizationState == LocalizationState.NOT_LOCALIZED) {

                    getLog().d("Localized.");
                    localizationState = LocalizationState.LOCALIZED;
                    handlerMain.post(() -> getView().onUpdateLocalized(true));
                }
            } else {
                if (localizationState == LocalizationState.UNKNOWN ||
                    localizationState == LocalizationState.LOCALIZED) {

                    getLog().d("Not localized.");
                    localizationState = LocalizationState.NOT_LOCALIZED;
                    handlerMain.post(() -> getView().onUpdateLocalized(false));
                }
            }
        }

        if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION &&
            pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
            debuggerAd2D.debug(pose);
        }

        if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE &&
            pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
            debuggerSs2D.debug(pose);
        }
    }

    public interface View {

        void onUpdateLocalized(boolean localized);

        void onUpdateAd2SsTranslation(double x, double y, double z);

        void onUpdateAd2DTranslation(double x, double y, double z);

        void onUpdateSs2DTranslation(double x, double y, double z);
    }

    private enum LocalizationState {
        UNKNOWN,
        LOCALIZED,
        NOT_LOCALIZED
    }

    private class PoseDebugger {

        PoseDebugPrinter printer;

        double prevTimestamp;

        double timeToUpdate;

        final Translation translation = new Translation();

        PoseDebugger(PoseDebugPrinter printer) {
            this.printer = printer;
        }

        void debug(TangoPoseData pose) {
            double timeDelta = (pose.timestamp - prevTimestamp) * SECS_TO_MILLISECS;
            prevTimestamp = pose.timestamp;

            timeToUpdate -= timeDelta;

            if (timeToUpdate < 0) {
                timeToUpdate = UPDATE_DEBUG_CONSOLE_INTERVAL;
                translation.x = pose.translation[0];
                translation.y = pose.translation[1];
                translation.z = pose.translation[2];
                handlerMain.post(() -> printer.print(this));
            }
        }
    }

    private class Translation {

        double x;

        double y;

        double z;
    }

    private interface PoseDebugPrinter {

        void print(PoseDebugger debugger);
    }
}
