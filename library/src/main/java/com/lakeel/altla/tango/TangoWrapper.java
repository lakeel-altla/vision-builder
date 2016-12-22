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
        // NOTE:
        //
        // 現状、TangoUX を用いるとデバッグ モードでは起動しなくなる。
        // これは、TangoUxLayout の配置の有無ではなく、TangoUx#start の実行により発生する。
        // このため、開発効率のために TangoUX を OFF にする場合には、TangoUx#start も止めなければならない。

        if (startTangoUx) {
            tangoUx.start(new TangoUx.StartParams());
        }

        tango = new Tango(context, new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Tango is ready.");

                // Synchronize against disconnecting while the service is being used in other threads.
                synchronized (this) {
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
                    } catch (TangoOutOfDateException e) {
                        if (tangoUx != null) {
                            tangoUx.showTangoOutOfDate();
                        }
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
        try {
            tangoUx.stop();
            tango.disconnect();
            connected = false;

            tangoUpdateDispatcher.getOnPoseAvailableListeners().clear();
            tangoUpdateDispatcher.getOnPointCloudAvailableListeners().clear();
            tangoUpdateDispatcher.getOnFrameAvailableListeners().clear();
            tangoUpdateDispatcher.getOnTangoEventListeners().clear();
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
