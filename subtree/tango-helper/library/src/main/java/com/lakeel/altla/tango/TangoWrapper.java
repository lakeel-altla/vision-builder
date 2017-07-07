package com.lakeel.altla.tango;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import com.projecttango.tangosupport.TangoSupport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TangoWrapper {

    private static final String TAG = TangoWrapper.class.getSimpleName();

    private static final DefaultTangoConfigFactory DEFAULT_TANGO_CONFIG_FACTORY = new DefaultTangoConfigFactory();

    private static final List<TangoCoordinateFramePair> DEFAULT_COORDINATE_FRAME_PAIR = Collections.emptyList();

    private final Context context;

    private final List<OnTangoReadyListener> onTangoReadyListeners = new ArrayList<>();

    private final List<OnTangoConnectErrorListener> onTangoConnectErrorListeners = new ArrayList<>();

    private final List<OnTangoDisconnectingListener> onTangoDisconnectingListeners = new ArrayList<>();

    private final List<OnTangoDisconnectedListener> onTangoDisconnectedListeners = new ArrayList<>();

    private final List<OnPoseAvailableListener> onPoseAvailableListeners = new ArrayList<>();

    private final List<OnPointCloudAvailableListener> onPointCloudAvailableListeners = new ArrayList<>();

    private final List<OnFrameAvailableListener> onFrameAvailableListeners = new ArrayList<>();

    private final List<OnTangoEventListener> onTangoEventListeners = new ArrayList<>();

    private final Tango.TangoUpdateCallback tangoUpdateCallback = new Tango.TangoUpdateCallback() {
        @Override
        public void onPoseAvailable(TangoPoseData pose) {
            for (OnPoseAvailableListener listener : onPoseAvailableListeners) {
                listener.onPoseAvailable(pose);
            }
        }

        @Override
        public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
        }

        @Override
        public void onFrameAvailable(int cameraId) {
            for (OnFrameAvailableListener listener : onFrameAvailableListeners) {
                listener.onFrameAvailable(cameraId);
            }
        }

        @Override
        public void onTangoEvent(TangoEvent event) {
            for (OnTangoEventListener listener : onTangoEventListeners) {
                listener.onTangoEvent(event);
            }
        }

        @Override
        public void onPointCloudAvailable(TangoPointCloudData pointCloud) {
            for (OnPointCloudAvailableListener listener : onPointCloudAvailableListeners) {
                listener.onPointCloudAvailable(pointCloud);
            }
        }
    };

    private boolean connected;

    private Tango tango;

    private boolean tangoSupportInitialized;

    private TangoConfigFactory tangoConfigFactory;

    private List<TangoCoordinateFramePair> coordinateFramePairs;

    public TangoWrapper(@NonNull Context context) {
        this.context = context;
    }

    public void setTangoConfigFactory(@Nullable TangoConfigFactory tangoConfigFactory) {
        this.tangoConfigFactory = tangoConfigFactory;
    }

    public void setCoordinateFramePairs(@Nullable List<TangoCoordinateFramePair> coordinateFramePairs) {
        this.coordinateFramePairs = coordinateFramePairs;
    }

    @NonNull
    public List<OnTangoReadyListener> getOnTangoReadyListeners() {
        return onTangoReadyListeners;
    }

    @NonNull
    public List<OnTangoConnectErrorListener> getOnTangoConnectErrorListeners() {
        return onTangoConnectErrorListeners;
    }

    @NonNull
    public List<OnTangoDisconnectingListener> getOnTangoDisconnectingListeners() {
        return onTangoDisconnectingListeners;
    }

    @NonNull
    public List<OnTangoDisconnectedListener> getOnTangoDisconnectedListeners() {
        return onTangoDisconnectedListeners;
    }

    @NonNull
    public List<OnPoseAvailableListener> getOnPoseAvailableListeners() {
        return onPoseAvailableListeners;
    }

    @NonNull
    public List<OnPointCloudAvailableListener> getOnPointCloudAvailableListeners() {
        return onPointCloudAvailableListeners;
    }

    @NonNull
    public List<OnFrameAvailableListener> getOnFrameAvailableListeners() {
        return onFrameAvailableListeners;
    }

    @NonNull
    public List<OnTangoEventListener> getOnTangoEventListeners() {
        return onTangoEventListeners;
    }

    public boolean isConnected() {
        return connected;
    }

    public Tango getTango() {
        return tango;
    }

    public void connect() {
        Log.d(TAG, "Connecting...");

        tango = new Tango(context, new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Tango is ready.");

                // Synchronize against disconnecting while the service is being used in other threads.
                synchronized (TangoWrapper.this) {
                    try {
                        if (!tangoSupportInitialized) {
                            TangoSupport.initialize();
                            tangoSupportInitialized = true;
                        }

                        final TangoConfig tangoConfig;
                        if (tangoConfigFactory == null) {
                            tangoConfig = DEFAULT_TANGO_CONFIG_FACTORY.create(tango);
                        } else {
                            tangoConfig = tangoConfigFactory.create(tango);
                        }
                        tango.connect(tangoConfig);

                        final List<TangoCoordinateFramePair> framePairs;
                        if (coordinateFramePairs == null) {
                            framePairs = DEFAULT_COORDINATE_FRAME_PAIR;
                        } else {
                            framePairs = coordinateFramePairs;
                        }
                        tango.connectListener(framePairs, tangoUpdateCallback);

                        connected = true;

                        Log.d(TAG, "Connected.");
                    } catch (TangoOutOfDateException e) {
                        Log.e(TAG, "Tango service outdated.", e);
                        raiseOnTangoConnectError(e);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, "Tango error occurred.", e);
                        raiseOnTangoConnectError(e);
                    } catch (TangoInvalidException e) {
                        Log.e(TAG, "Invalid UUID error occurred.", e);
                        raiseOnTangoConnectError(e);
                    } catch (TangoException e) {
                        Log.e(TAG, "Unexpected tango error occurred.", e);
                        raiseOnTangoConnectError(e);
                    }

                    if (!onTangoReadyListeners.isEmpty()) {
                        for (OnTangoReadyListener listener : onTangoReadyListeners) {
                            listener.onTangoReady(tango);
                        }
                    }
                }
            }
        });
    }

    public synchronized void disconnect() {
        Log.d(TAG, "Disconnecting...");

        if (!onTangoDisconnectingListeners.isEmpty()) {
            for (OnTangoDisconnectingListener listener : onTangoDisconnectingListeners) {
                listener.onTangoDisconnecting();
            }
        }

        try {
            if (tango != null) {
                tango.disconnect();
            }
        } catch (TangoErrorException e) {
            Log.e(TAG, "Tango error occurred.", e);
        }

        connected = false;

        Log.d(TAG, "Disconnected.");

        if (!onTangoDisconnectedListeners.isEmpty()) {
            for (OnTangoDisconnectedListener listener : onTangoDisconnectedListeners) {
                listener.onTangoDisconnected();
            }
        }
    }

    private void raiseOnTangoConnectError(@NonNull TangoException e) {
        if (!onTangoConnectErrorListeners.isEmpty()) {
            for (OnTangoConnectErrorListener listener : onTangoConnectErrorListeners) {
                listener.onTangoConnectError(e);
            }
        }
    }

    public interface TangoConfigFactory {

        TangoConfig create(Tango tango);
    }

    public interface OnTangoReadyListener {

        void onTangoReady(Tango tango);
    }

    public interface OnTangoConnectErrorListener {

        void onTangoConnectError(TangoException e);
    }

    public interface OnTangoDisconnectingListener {

        void onTangoDisconnecting();
    }

    public interface OnTangoDisconnectedListener {

        void onTangoDisconnected();
    }

    public interface OnFrameAvailableListener {

        void onFrameAvailable(int cameraId);
    }

    public interface OnPointCloudAvailableListener {

        void onPointCloudAvailable(TangoPointCloudData pointCloud);
    }

    public interface OnPoseAvailableListener {

        void onPoseAvailable(TangoPoseData pose);
    }

    public interface OnTangoEventListener {

        void onTangoEvent(TangoEvent event);
    }

    private static final class DefaultTangoConfigFactory implements TangoConfigFactory {

        @Override
        public TangoConfig create(Tango tango) {
            return tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        }
    }
}
