package com.lakeel.altla.vision.builder.presentation.view.renderer;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.tango.rajawali.TangoCameraRenderer;
import com.lakeel.altla.vision.builder.presentation.graphics.XyzAxesBuilder;
import com.lakeel.altla.vision.builder.presentation.model.ActorModel;
import com.lakeel.altla.vision.builder.presentation.model.EditAxesModel;
import com.lakeel.altla.vision.model.Scope;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class MainRenderer extends TangoCameraRenderer implements OnObjectPickedListener {

    private static final Log LOG = LogFactory.getLog(MainRenderer.class);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Queue<ActorModel> addActorModelQueue = new LinkedList<>();

    private final Queue<ActorModel> updateActorModelQueue = new LinkedList<>();

    private final Queue<String> deleteActorIdQueue = new LinkedList<>();

    private final Object editAxesModelLock = new Object();

    private final Map<Object3D, ActorModel> actorModelMap = new HashMap<>();

    private final Map<String, Object3D> object3DMap = new HashMap<>();

    private final Map<String, ObjectModel> objectModelMap = new HashMap<>();

    private final Object clearAllActorsLock = new Object();

    private ObjectColorPicker objectColorPicker;

    private Line3D axes;

    private EditAxesModel editAxesModel;

    private OnActorPickedListener onActorPickedListener;

    private OnCurrentCameraTransformUpdatedListener onCurrentCameraTransformUpdatedListener;

    private boolean clearAllActors;

    private boolean tangoLocalized;

    public MainRenderer(Context context) {
        super(context);
    }

    @Override
    protected void initSceneOverride() {
        super.initSceneOverride();

        objectColorPicker = new ObjectColorPicker(this);
        objectColorPicker.setOnObjectPickedListener(this);

        // Build axes model indicating a pose of a picked object.
        axes = new XyzAxesBuilder().setThickness(5)
                                   .setLength(0.25f)
                                   .build();
        // Shift the axes model to a little camera beside.
        axes.setPosition(0, 0, -0.001f);
        axes.setVisible(false);
        getCurrentScene().addChild(axes);
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        // NOTE:
        //
        // Adding primitives into a scene in a non-OpenGL thread fail,
        // because its constructors access OpenGL vertex buffers etc.
        // So we queue a data indicating a primitive in a non-OpenGL,
        // Renderer#onRender invoked in OpenGL thread instantiates an actual primitive.
        //

        if (tangoLocalized) {
            // Add user actors.
            synchronized (addActorModelQueue) {
                while (true) {
                    ActorModel model = addActorModelQueue.poll();
                    if (model == null) {
                        break;
                    }

                    Object3D object3D = model.create();

                    getCurrentScene().addChild(object3D);
                    objectColorPicker.registerObject(object3D);
                    actorModelMap.put(object3D, model);
                    object3DMap.put(model.actor.getId(), object3D);
                }
            }

            // Update user actors.
            synchronized (updateActorModelQueue) {
                while (true) {
                    ActorModel model = updateActorModelQueue.poll();
                    if (model == null) {
                        break;
                    }

                    Object3D object3D = object3DMap.get(model.actor.getId());
                    if (object3D == null) {
                        LOG.e("Object3D not found: actorId = %s", model.actor.getId());
                        continue;
                    }

                    model.update(object3D);
                }
            }
        }

        // Update axes for edit.
        synchronized (editAxesModelLock) {
            if (editAxesModel != null) {
                axes.setPosition(editAxesModel.position);
                axes.setOrientation(editAxesModel.orientation);

                editAxesModel = null;
            }
        }

        // Delete actors.
        synchronized (deleteActorIdQueue) {
            while (true) {
                String actorId = deleteActorIdQueue.poll();
                if (actorId == null) {
                    break;
                }

                Object3D object3D = object3DMap.remove(actorId);
                if (object3D == null) {
                    continue;
                }

                actorModelMap.remove(object3D);

                getCurrentScene().removeChild(object3D);
            }
        }

        // Clear all actors.
        synchronized (clearAllActorsLock) {
            if (clearAllActors) {
                clearAllActors = false;

                Set<String> actorIds = new HashSet<>(object3DMap.keySet());
                for (String actorId : actorIds) {
                    Object3D object3D = object3DMap.remove(actorId);
                    if (object3D == null) {
                        continue;
                    }

                    actorModelMap.remove(object3D);

                    getCurrentScene().removeChild(object3D);
                }
            }
        }

        super.onRender(ellapsedRealtime, deltaTime);
    }

    @Override
    public void onObjectPicked(@NonNull Object3D object) {
        changePickedObject(object);
    }

    @Override
    public void onNoObjectPicked() {
        // Unpick
        changePickedObject(null);
    }

    @Override
    protected void onCurrentCameraTransformUpdated(double timestamp) {
        super.onCurrentCameraTransformUpdated(timestamp);

        Camera camera = getCurrentCamera();
        Vector3 position = camera.getPosition();
        Quaternion orientation = camera.getOrientation();
        Vector3 forward = getCurrentCameraForward();

        raiseOnCurrentCameraTransformUpdated(position.x, position.y, position.z,
                                             orientation.x, orientation.y, orientation.z, orientation.w,
                                             forward.x, forward.y, forward.z);
    }

    public void setTangoLocalized(boolean tangoLocalized) {
        this.tangoLocalized = tangoLocalized;
    }

    // This method must be invoke on the main thread.
    public void setOnCurrentCameraTransformUpdatedListener(
            OnCurrentCameraTransformUpdatedListener onCurrentCameraTransformUpdatedListener) {
        this.onCurrentCameraTransformUpdatedListener = onCurrentCameraTransformUpdatedListener;
    }

    // This method must be invoke on the main thread.
    public void setOnActorPickedListener(@Nullable OnActorPickedListener onActorPickedListener) {
        this.onActorPickedListener = onActorPickedListener;
    }

    public void addActorModel(@NonNull ActorModel actorModel) {
        LOG.v("Added ActorModel: actorId = %s", actorModel.actor.getId());

        synchronized (addActorModelQueue) {
            addActorModelQueue.add(actorModel);
        }
    }

    public void updateActorModel(@NonNull ActorModel actorModel) {
        synchronized (updateActorModelQueue) {
            updateActorModelQueue.add(actorModel);
        }
    }

    public void updateEditAxesModel(@NonNull EditAxesModel editAxesModel) {
        synchronized (editAxesModelLock) {
            this.editAxesModel = editAxesModel;
        }
    }

    public void removeActor(@NonNull String actorId) {
        synchronized (deleteActorIdQueue) {
            deleteActorIdQueue.add(actorId);
        }
    }

    public void clearAllActors() {
        synchronized (clearAllActorsLock) {
            clearAllActors = true;
        }
    }

    public void tryPickObject(float x, float y) {
        LOG.d("tryPickObject: x = %f, y = %f", x, y);
        objectColorPicker.getObjectAt(x, y);
    }

    private void changePickedObject(@Nullable Object3D object) {
        ActorModel pickedActor = null;

        if (object != null) {
            // The axes model uses the same pose with the picked object.
            axes.setPosition(object.getPosition().clone());
            axes.setOrientation(object.getOrientation().clone());
            axes.setVisible(true);

            pickedActor = actorModelMap.get(object);
        } else {
            axes.setVisible(false);
        }

        raiseOnActorPicked(pickedActor);
    }

    private void raiseOnCurrentCameraTransformUpdated(double positionX, double positionY, double positionZ,
                                                      double orientationX, double orientationY, double orientationZ,
                                                      double orientationW,
                                                      double forwardX, double forwardY, double forwardZ) {
        mainHandler.post(() -> {
            if (onCurrentCameraTransformUpdatedListener != null) {
                onCurrentCameraTransformUpdatedListener.onCurrentCameraTransformUpdated(
                        positionX, positionY, positionZ,
                        orientationX, orientationY, orientationZ, orientationW,
                        forwardX, forwardY, forwardZ
                );
            }
        });
    }

    private void raiseOnActorPicked(@Nullable ActorModel actorModel) {
        mainHandler.post(() -> {
            if (onActorPickedListener != null) {
                onActorPickedListener.onActorPicked(actorModel);
            }
        });
    }

    public interface OnCurrentCameraTransformUpdatedListener {

        void onCurrentCameraTransformUpdated(double positionX, double positionY, double positionZ,
                                             double orientationX, double orientationY, double orientationZ,
                                             double orientationW,
                                             double forwardX, double forwardY, double forwardZ);
    }

    public interface OnActorPickedListener {

        void onActorPicked(@Nullable ActorModel actorModel);
    }

    private final class ObjectModel {

        final Scope scope;

        final String actorId;

        final Object3D object3D;

        private ObjectModel(@NonNull Scope scope, @NonNull String actorId, @NonNull Object3D object3D) {
            this.scope = scope;
            this.actorId = actorId;
            this.object3D = object3D;
        }
    }
}
