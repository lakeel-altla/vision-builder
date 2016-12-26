package com.lakeel.altla.tango;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoOutOfDateException;

import com.projecttango.tangosupport.TangoSupport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public final class TangoWrapper {

    private static final String TAG = TangoWrapper.class.getSimpleName();

    private static final DefaultTangoConfigFactory DEFAULT_TANGO_CONFIG_FACTORY = new DefaultTangoConfigFactory();

    private final Context context;

    private final TangoUx tangoUx;

    private final TangoUpdateDispatcher tangoUpdateDispatcher = new TangoUpdateDispatcher();

    private boolean connected;

    private Tango tango;

    private boolean tangoSupportInitialized;

    private TangoConfigFactory tangoConfigFactory;

    private List<TangoCoordinateFramePair> coordinateFramePairs;

    private boolean startTangoUx;

    private List<OnTangoReadyListener> onTangoReadyListeners = new LinkedList<>();

    public TangoWrapper(@NonNull Context context) {
        this.context = context;
        tangoUx = new TangoUx(context);
    }

    public void setTangoConfigFactory(TangoConfigFactory tangoConfigFactory) {
        this.tangoConfigFactory = tangoConfigFactory;
    }

    public void setCoordinateFramePairs(List<TangoCoordinateFramePair> coordinateFramePairs) {
        this.coordinateFramePairs = coordinateFramePairs;
    }

    public void setStartTangoUx(boolean startTangoUx) {
        this.startTangoUx = startTangoUx;
    }

    public boolean isConnected() {
        return connected;
    }

    public Tango getTango() {
        return tango;
    }

    public TangoUx getTangoUx() {
        return tangoUx;
    }

    public synchronized void addOnPoseAvailableListener(@NonNull OnPoseAvailableListener listener) {
        tangoUpdateDispatcher.getOnPoseAvailableListeners().add(listener);
    }

    public synchronized void removeOnPoseAvailableListener(@NonNull OnPoseAvailableListener listener) {
        tangoUpdateDispatcher.getOnPoseAvailableListeners().remove(listener);
    }

    public synchronized void addOnPointCloudAvailableListener(@NonNull OnPointCloudAvailableListener listener) {
        tangoUpdateDispatcher.getOnPointCloudAvailableListeners().add(listener);
    }

    public synchronized void removeOnPointCloudAvailableListener(@NonNull OnPointCloudAvailableListener listener) {
        tangoUpdateDispatcher.getOnPointCloudAvailableListeners().remove(listener);
    }

    public synchronized void addOnFrameAvailableListener(@NonNull OnFrameAvailableListener listener) {
        tangoUpdateDispatcher.getOnFrameAvailableListeners().add(listener);
    }

    public synchronized void removeOnFrameAvailableListener(@NonNull OnFrameAvailableListener listener) {
        tangoUpdateDispatcher.getOnFrameAvailableListeners().remove(listener);
    }

    public synchronized void addOnTangoEventListener(@NonNull OnTangoEventListener listener) {
        tangoUpdateDispatcher.getOnTangoEventListeners().add(listener);
    }

    public synchronized void removeOnTangoEventListener(@NonNull OnTangoEventListener listener) {
        tangoUpdateDispatcher.getOnTangoEventListeners().remove(listener);
    }

    public synchronized void addOnTangoReadyListener(@NonNull OnTangoReadyListener listener) {
        onTangoReadyListeners.add(listener);

        if (connected) {
            listener.onTangoReady(tango);
        }
    }

    public synchronized void removeOnTangoReadyListener(@NonNull OnTangoReadyListener listener) {
        onTangoReadyListeners.remove(listener);
    }

    public void connect() {
        Log.d(TAG, "Connecting...");

        if (startTangoUx) {
            tangoUx.start(new TangoUx.StartParams());
        }

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

                        TangoConfig tangoConfig;
                        if (tangoConfigFactory == null) {
                            tangoConfig = DEFAULT_TANGO_CONFIG_FACTORY.create(tango);
                        } else {
                            tangoConfig = tangoConfigFactory.create(tango);
                        }
                        tango.connect(tangoConfig);

                        TangoUxListener tangoUxListener = new TangoUxListener(tangoUx);
                        tangoUpdateDispatcher.getOnPoseAvailableListeners().add(tangoUxListener);
                        tangoUpdateDispatcher.getOnTangoEventListeners().add(tangoUxListener);
                        tango.connectListener(coordinateFramePairs, tangoUpdateDispatcher);

                        connected = true;

                        Log.d(TAG, "Connected.");
                    } catch (TangoOutOfDateException e) {
                        tangoUx.showTangoOutOfDate();

                        Log.e(TAG, "Tango service outdated.", e);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, "Tango error occurred.", e);
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

        try {
            tangoUx.stop();
            tango.disconnect();

            tangoUpdateDispatcher.getOnPoseAvailableListeners().clear();
            tangoUpdateDispatcher.getOnPointCloudAvailableListeners().clear();
            tangoUpdateDispatcher.getOnFrameAvailableListeners().clear();
            tangoUpdateDispatcher.getOnTangoEventListeners().clear();

            connected = false;

            Log.d(TAG, "Disconnected.");
        } catch (TangoErrorException e) {
            Log.e(TAG, "Tango error occurred.", e);
        }
    }

    public interface OnTangoReadyListener {

        void onTangoReady(Tango tango);
    }

    public interface TangoConfigFactory {

        TangoConfig create(Tango tango);
    }

    private static final class DefaultTangoConfigFactory implements TangoConfigFactory {

        @Override
        public TangoConfig create(Tango tango) {
            return tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        }
    }
}
