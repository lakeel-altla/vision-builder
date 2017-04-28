package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoPoseData;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.rajawali.pool.Pool;
import com.lakeel.altla.rajawali.pool.QuaternionPool;
import com.lakeel.altla.rajawali.pool.Vector3Pool;
import com.lakeel.altla.tango.OnFrameAvailableListener;
import com.lakeel.altla.tango.OnPoseAvailableListener;
import com.lakeel.altla.tango.TangoWrapper;
import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.module.Names;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowSettingsViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.SnackbarEvent;
import com.lakeel.altla.vision.builder.presentation.model.ActorDragConstants;
import com.lakeel.altla.vision.builder.presentation.model.ActorEditMode;
import com.lakeel.altla.vision.builder.presentation.model.ActorModel;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.builder.presentation.model.EditAxesModel;
import com.lakeel.altla.vision.builder.presentation.model.ImageActorModel;
import com.lakeel.altla.vision.builder.presentation.model.TangoLocalizationState;
import com.lakeel.altla.vision.builder.presentation.view.renderer.MainRenderer;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Asset;
import com.lakeel.altla.vision.model.AssetType;
import com.lakeel.altla.vision.model.Layer;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.ISurfaceRenderer;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Defines the presenter for {@link View}.
 */
public final class ArPresenter extends BasePresenter<ArPresenter.View>
        implements TangoWrapper.OnTangoReadyListener,
                   OnFrameAvailableListener,
                   OnPoseAvailableListener,
                   MainRenderer.OnCurrentCameraTransformUpdatedListener,
                   MainRenderer.OnActorPickedListener {

    private static final float ACTOR_DROP_POSITION_ADJUSTMENT = 2f;

    private static final float TRANSLATE_OBJECT_DISTANCE_SCALE = 0.005f;

    private static final float ROTATE_OBJECT_ANGLE_SCALE = 1f;

    private static final float SCALE_OBJECT_SIZE_SCALE = 0.5f;

    private static final List<TangoCoordinateFramePair> FRAME_PAIRS;

    static {
        FRAME_PAIRS = new ArrayList<>();

        FRAME_PAIRS.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                                                     TangoPoseData.COORDINATE_FRAME_DEVICE));

        FRAME_PAIRS.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                                                     TangoPoseData.COORDINATE_FRAME_DEVICE));

        FRAME_PAIRS.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                                                     TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
    }

    @Named(Names.ACTIVITY_CONTEXT)
    @Inject
    Context context;

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    @Inject
    ArModel arModel;

    public final RelayCommand commandShowSettings = new RelayCommand(this::showSettingsView);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Vector3 cameraPosition = new Vector3();

    private final Quaternion cameraOrientation = new Quaternion();

    private final Vector3 cameraForward = new Vector3();

    private TangoLocalizationState tangoLocalizationState;

    private ActorManager actorManager;

    private MainRenderer renderer;

    private ActorModel pickedActorModel;

    private volatile boolean active = true;

    private ActorEditMode actorEditMode = ActorEditMode.NONE;

    private Axis translateAxis = Axis.X;

    private Axis rotateAxis = Axis.Y;

    @Inject
    public ArPresenter() {
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        visionService.getTangoWrapper().setStartTangoUx(false);
        visionService.getTangoWrapper().setCoordinateFramePairs(FRAME_PAIRS);
        visionService.getTangoWrapper().setTangoConfigFactory(this::createTangoConfig);
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        getView().setTangoUxLayout(visionService.getTangoWrapper().getTangoUx());

        renderer = new MainRenderer(context);
        renderer.setOnCurrentCameraTransformUpdatedListener(this);
        renderer.setOnActorPickedListener(this);
        getView().setSurfaceRenderer(renderer);
        getView().pauseTextureView();

        getView().onUpdateObjectMenuVisible(false);
        getView().onUpdateTranslateSelected(false);
        getView().onUpdateRotateSelected(false);
        getView().onUpdateTranslateMenuVisible(false);
        getView().onUpdateRotateMenuVisible(false);
        getView().onUpdateTranslateAxisSelected(Axis.X, true);
        getView().onUpdateRotateAxisSelected(Axis.Y, true);

        eventBus.post(ActionBarVisibleEvent.INVISIBLE);
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        // Instantiate ActorManager here to clear the bitmap cache in Picasso.
        actorManager = new ActorManager(context);

        eventBus.register(this);
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        compositeDisposable.clear();

        eventBus.unregister(this);
    }

    @Override
    protected void onResumeOverride() {
        super.onResumeOverride();

        tangoLocalizationState = TangoLocalizationState.UNKNOWN;
        getView().onUpdateImageButtonAssetListVisible(false);

        connectTango();
    }

    @Override
    protected void onPauseOverride() {
        super.onPauseOverride();

        disconnectTango();

        // Pause the thread for OpenGL in the texture view.
        getView().pauseTextureView();
        getView().onUpdateImageButtonAssetListVisible(true);
    }

    @Override
    public void onTangoReady(Tango tango) {
        renderer.connectToTangoCamera(tango);

        // Resume the texture view after the tango becomes ready.
        getView().resumeTextureView();

        final AreaSettings areaSettings = arModel.getAreaSettings();

        if (areaSettings != null) {
            if (areaSettings.getAreaId() == null) {
                throw new IllegalStateException("Unknown areaId: null");
            }

            runOnUiThread(() -> {
                getView().onUpdateImageButtonAssetListVisible(true);
            });

            Disposable disposable = arModel
                    .loadActors()
                    .subscribe(actor -> {
                        actorManager.addActor(actor);
                    }, e -> {
                        getLog().e("Failed.", e);
                        getView().showSnackbar(R.string.snackbar_failed);
                    });
            compositeDisposable.add(disposable);
        }
    }

    @Override
    public void onFrameAvailable(int cameraId) {
        if (active && cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
            renderer.onFrameAvailable();
        }
    }

    @Override
    public void onPoseAvailable(TangoPoseData pose) {
        if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION &&
            pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE) {

            if (pose.statusCode == TangoPoseData.POSE_VALID) {
                if (tangoLocalizationState == TangoLocalizationState.UNKNOWN ||
                    tangoLocalizationState == TangoLocalizationState.NOT_LOCALIZED) {

                    getLog().d("Localized.");
                    tangoLocalizationState = TangoLocalizationState.LOCALIZED;
                    renderer.setTangoLocalized(true);
                }
            } else {
                if (tangoLocalizationState == TangoLocalizationState.UNKNOWN ||
                    tangoLocalizationState == TangoLocalizationState.LOCALIZED) {

                    getLog().d("Not localized.");
                    tangoLocalizationState = TangoLocalizationState.NOT_LOCALIZED;
                    renderer.setTangoLocalized(false);
                }
            }
        }
    }

    @Override
    public void onCurrentCameraTransformUpdated(double positionX, double positionY, double positionZ,
                                                double orientationX, double orientationY, double orientationZ,
                                                double orientationW, double forwardX, double forwardY,
                                                double forwardZ) {
        cameraPosition.setAll(positionX, positionY, positionZ);
        cameraOrientation.setAll(orientationW, orientationX, orientationY, orientationZ);
        cameraForward.setAll(forwardX, forwardY, forwardZ);
    }

    @Override
    public void onActorPicked(@Nullable ActorModel actorModel) {
        if (arModel.getAreaSettings() != null) {
            pickedActorModel = actorModel;

            final boolean picked = (pickedActorModel != null);
            if (picked) {
                // picked.
                arModel.setSelectedActor(pickedActorModel.actor);
            } else {
                // unpicked.
                arModel.setSelectedActor(null);
            }

            getView().setActorViewVisible(picked);
            getView().setMainMenuVisible(!picked);
        }
    }

    public void showAreaSettingsView() {
        eventBus.post(ShowAreaSettingsViewEvent.INSTANCE);
    }

    public void showAssetListView() {
        getView().onUpdateAssetListVisible(true);
        getView().setMainMenuVisible(false);
    }

    public void onTouchButtonTranslate() {
        actorEditMode = ActorEditMode.TRANSLATE;

        getView().onUpdateTranslateSelected(true);
        getView().onUpdateTranslateMenuVisible(true);
        getView().onUpdateRotateSelected(false);
        getView().onUpdateRotateMenuVisible(false);
        getView().onUpdateScaleSelected(false);
    }

    public void onTouchButtonRotateObject() {
        actorEditMode = ActorEditMode.ROTATE;

        getView().onUpdateTranslateSelected(false);
        getView().onUpdateTranslateMenuVisible(false);
        getView().onUpdateRotateSelected(true);
        getView().onUpdateRotateMenuVisible(true);
        getView().onUpdateScaleSelected(false);
    }

    public void onTouchButtonTranslateAxis(Axis axis) {
        translateAxis = axis;

        getView().onUpdateTranslateAxisSelected(Axis.X, axis == Axis.X);
        getView().onUpdateTranslateAxisSelected(Axis.Y, axis == Axis.Y);
        getView().onUpdateTranslateAxisSelected(Axis.Z, axis == Axis.Z);
    }

    public void onTouchButtonRotateAxis(Axis axis) {
        rotateAxis = axis;

        getView().onUpdateRotateAxisSelected(Axis.X, axis == Axis.X);
        getView().onUpdateRotateAxisSelected(Axis.Y, axis == Axis.Y);
        getView().onUpdateRotateAxisSelected(Axis.Z, axis == Axis.Z);
    }

    public void onTouchButtonScale() {
        actorEditMode = ActorEditMode.SCALE;

        getView().onUpdateTranslateSelected(false);
        getView().onUpdateTranslateMenuVisible(false);
        getView().onUpdateRotateSelected(false);
        getView().onUpdateRotateMenuVisible(false);
        getView().onUpdateScaleSelected(true);
    }

    public void onTouchButtonDetail() {
//        if (pickedActorModel == null) return;
//
//        getView().onShowUserActorView(pickedActorModel.actor.getAreaId(), pickedActorModel.actor.getId());
    }

    public void onClickButtonDelete() {
        if (pickedActorModel == null) return;

        // TODO: confirmation.

        actorManager.removeActor(pickedActorModel.actor.getId());

        visionService.getUserActorApi().deleteActorById(pickedActorModel.actor.getId());
    }

    public void onDropModel(@NonNull ClipData clipData) {
        if (clipData.getItemCount() == 0) throw new IllegalStateException("No item.");

        Intent intent = clipData.getItemAt(0).getIntent();
        if (intent == null) throw new IllegalStateException("No intent.");

        Bundle bundle = intent.getExtras();
        bundle.setClassLoader(Parcels.class.getClassLoader());
        Asset asset = Parcels.unwrap(bundle.getParcelable(ActorDragConstants.INTENT_EXTRA_ASSET));
        if (asset == null) throw new IllegalStateException("No actor.");

        Actor actor = new Actor();
        actor.setUserId(CurrentUser.getInstance().getUserId());
        actor.setScopeAsEnum(Scope.USER);
        actor.setAreaId(arModel.getAreaSettings().getAreaId());
        // TODO: handle other asset types.
        actor.setAssetTypeAsEnum(AssetType.IMAGE);
        actor.setAssetId(asset.getId());
        // TODO: commercial too.
        actor.setLayerAsEnum(Layer.NONCOMMERCIAL);
        actor.setName(asset.getName());

        // Decide the position and the orientation of the dropped user actor.
        try (Pool.Holder<Vector3> positionHolder = Vector3Pool.get();
             Pool.Holder<Quaternion> orientationHolder = QuaternionPool.get();
             Pool.Holder<Vector3> translationHolder = Vector3Pool.get();
             Pool.Holder<Vector3> cameraBackwardHolder = Vector3Pool.get()) {

            Vector3 position = positionHolder.get();
            Quaternion orientation = orientationHolder.get();
            Vector3 translation = translationHolder.get();
            Vector3 cameraBackward = cameraBackwardHolder.get();

            position.setAll(cameraPosition);

            translation.setAll(cameraForward);
            translation.multiply(ACTOR_DROP_POSITION_ADJUSTMENT);

            position.add(translation);

            cameraBackward.setAll(cameraForward);
            cameraBackward.inverse();

            orientation.lookAt(cameraBackward, Vector3.Y);

            actor.setPositionX(position.x);
            actor.setPositionY(position.y);
            actor.setPositionZ(position.z);
            actor.setOrientationX(orientation.x);
            actor.setOrientationY(orientation.y);
            actor.setOrientationZ(orientation.z);
            actor.setOrientationW(orientation.w);
        }

        // Add it into the memory.
        actorManager.addActor(actor);

        // Add it into the server.
        visionService.getUserActorApi().saveActor(actor);
    }

    public boolean onSingleTapUp(MotionEvent e) {
        renderer.tryPickObject(e.getX(), e.getY());
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (pickedActorModel != null) {
            if (actorEditMode == ActorEditMode.TRANSLATE) {
                if (Math.abs(distanceY) < Math.abs(distanceX)) {
                    translateUserActor(distanceX);
                } else {
                    translateUserActor(distanceY);
                }
            } else if (actorEditMode == ActorEditMode.ROTATE) {
                if (Math.abs(distanceY) < Math.abs(distanceX)) {
                    rotateUserActor(distanceX);
                } else {
                    rotateUserActor(distanceY);
                }
            } else if (actorEditMode == ActorEditMode.SCALE) {
                if (Math.abs(distanceY) < Math.abs(distanceX)) {
                    scaleUserActor(distanceX);
                } else {
                    scaleUserActor(distanceY);
                }
            }

            return true;
        }
        return false;
    }

    public void onScrollFinished(MotionEvent event) {
        if (pickedActorModel == null) return;

        // Save.
        visionService.getUserActorApi().saveActor(pickedActorModel.actor);
    }

    @Subscribe
    public void onEvent(@NonNull ActorPresenter.ActorViewVisibleEvent event) {
        getView().setActorViewVisible(false);
        getView().setMainMenuVisible(true);
    }

    @Subscribe
    public void onEvent(@NonNull SnackbarEvent event) {
        getView().showSnackbar(event.resource);
    }

    @NonNull
    private TangoConfig createTangoConfig(@NonNull Tango tango) {
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);

        // NOTE:
        // Low latency integration is necessary to achieve a precise alignment of
        // virtual objects with the RBG image and produce a good AR effect.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        // Enable the color camera.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        // NOTE:
        // Javadoc says, "LEARNINGMODE and loading AREADESCRIPTION cannot be used if drift correction is enabled."
//        config.putBoolean(TangoConfig.KEY_BOOLEAN_DRIFT_CORRECTION, true);

        final AreaSettings areaSettings = arModel.getAreaSettings();
        if (areaSettings != null && areaSettings.getAreaDescriptionId() != null) {
            config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, areaSettings.getAreaDescriptionId());
        }

        return config;
    }

    private void connectTango() {
        visionService.getTangoWrapper().addOnTangoReadyListener(this);
        visionService.getTangoWrapper().addOnFrameAvailableListener(this);
        visionService.getTangoWrapper().addOnPoseAvailableListener(this);
        visionService.getTangoWrapper().connect();
        active = true;
    }

    private void disconnectTango() {
        active = false;
        renderer.clearAllActors();
        renderer.disconnectFromTangoCamera();
        visionService.getTangoWrapper().removeOnTangoReadyListener(this);
        visionService.getTangoWrapper().removeOnFrameAvailableListener(this);
        visionService.getTangoWrapper().removeOnPoseAvailableListener(this);
        visionService.getTangoWrapper().disconnect();
    }

    private void showSettingsView() {
        eventBus.post(ShowSettingsViewEvent.INSTANCE);
    }

    private void translateUserActor(float distance) {
        // Scale.
        float scaledDistance = distance * TRANSLATE_OBJECT_DISTANCE_SCALE;

        // Translate.
        try (Pool.Holder<Vector3> translationHolder = Vector3Pool.get();
             Pool.Holder<Quaternion> rotationHolder = QuaternionPool.get();
             Pool.Holder<Vector3> positionHolder = Vector3Pool.get()) {

            Vector3 translation = translationHolder.get();
            Quaternion rotation = rotationHolder.get();
            Vector3 position = positionHolder.get();

            switch (translateAxis) {
                case X:
                    translation.setAll(Vector3.X);
                    break;
                case Y:
                    translation.setAll(Vector3.Y);
                    break;
                case Z:
                default:
                    translation.setAll(Vector3.Z);
                    break;
            }

            pickedActorModel.setOrientationTo(rotation);
            // Conjugate because rotateBy is wrong...
            rotation.conjugate();

            translation.rotateBy(rotation);
            translation.normalize();
            translation.multiply(scaledDistance);

            pickedActorModel.setPositionTo(position);
            position.add(translation);

            pickedActorModel.setPosition(position);
        }

        EditAxesModel editAxesModel = new EditAxesModel();
        pickedActorModel.setPositionTo(editAxesModel.position);
        pickedActorModel.setOrientationTo(editAxesModel.orientation);

        renderer.updateActorModel(pickedActorModel);
        renderer.updateEditAxesModel(editAxesModel);
    }

    private void rotateUserActor(float angle) {
        // Scale.
        float scaledAngle = angle * ROTATE_OBJECT_ANGLE_SCALE;

        // Rotate.
        try (Pool.Holder<Vector3> baseAxisHolder = Vector3Pool.get();
             Pool.Holder<Vector3> modelAxisHolder = Vector3Pool.get();
             Pool.Holder<Quaternion> rotationHolder = QuaternionPool.get();
             Pool.Holder<Quaternion> axisRotationHolder = QuaternionPool.get();
             Pool.Holder<Quaternion> actorOrientationHolder = QuaternionPool.get()) {

            Vector3 baseAxis = baseAxisHolder.get();
            Vector3 modelAxis = modelAxisHolder.get();
            Quaternion rotation = rotationHolder.get();
            Quaternion axisRotation = axisRotationHolder.get();
            Quaternion actorOrientation = actorOrientationHolder.get();

            switch (rotateAxis) {
                case X:
                    baseAxis.setAll(Vector3.X);
                    break;
                case Y:
                    baseAxis.setAll(Vector3.Y);
                    break;
                case Z:
                default:
                    baseAxis.setAll(Vector3.Z);
                    break;
            }

            modelAxis.setAll(baseAxis);
            pickedActorModel.setOrientationTo(rotation);
            // Conjugate because rotateBy is wrong...
            rotation.conjugate();
            modelAxis.rotateBy(rotation);

            axisRotation.fromAngleAxis(modelAxis, scaledAngle);
            pickedActorModel.setOrientationTo(actorOrientation);
            actorOrientation.multiply(axisRotation);
            pickedActorModel.setOrientation(actorOrientation);
        }

        EditAxesModel editAxesModel = new EditAxesModel();
        pickedActorModel.setPositionTo(editAxesModel.position);
        pickedActorModel.setOrientationTo(editAxesModel.orientation);

        renderer.updateActorModel(pickedActorModel);
        renderer.updateEditAxesModel(editAxesModel);
    }

    private void scaleUserActor(float size) {

        // Scale the raw value.
        float scaledSize = size * SCALE_OBJECT_SIZE_SCALE;

        final float MIN_RATIO = 0.1f;

        try (Pool.Holder<Vector3> scaleHolder = Vector3Pool.get()) {
            Vector3 scale = scaleHolder.get();

            float ratio;
            if (0 < scaledSize) {
                ratio = 1 + scaledSize * 0.01f;
            } else {
                ratio = Math.max(1 - Math.abs(scaledSize) * 0.01f, MIN_RATIO);
            }

            pickedActorModel.setScaleTo(scale);
            scale.multiply(ratio);

            pickedActorModel.setScale(scale);
        }

        renderer.updateActorModel(pickedActorModel);
    }

    public interface View {

        void setTangoUxLayout(TangoUx tangoUx);

        void setSurfaceRenderer(ISurfaceRenderer renderer);

        void resumeTextureView();

        void pauseTextureView();

        void setMainMenuVisible(boolean visible);

        void onUpdateImageButtonAssetListVisible(boolean visible);

        void setActorViewVisible(boolean visible);

        void onUpdateAssetListVisible(boolean visible);

        void onUpdateObjectMenuVisible(boolean visible);

        void onUpdateTranslateSelected(boolean selected);

        void onUpdateTranslateMenuVisible(boolean visible);

        void onUpdateTranslateAxisSelected(Axis axis, boolean selected);

        void onUpdateRotateSelected(boolean selected);

        void onUpdateRotateMenuVisible(boolean visible);

        void onUpdateRotateAxisSelected(Axis axis, boolean selected);

        void onUpdateScaleSelected(boolean selected);

        void showSnackbar(@StringRes int resId);
    }

    private final class ActorManager {

        final Set<Target> targetMap = new HashSet<>();

        Picasso picasso;

        ActorManager(@NonNull Context context) {
            picasso = new Picasso.Builder(context).build();
        }

        void addActor(@NonNull Actor actor) {
            getLog().v("Adding the actor: id = %s", actor.getId());

            switch (actor.getAssetTypeAsEnum()) {
                case IMAGE:
                    addImageActor(actor);
                    break;
                default:
                    getLog().e("Unexpected asset type: actorId = %s, assetType = %s",
                               actor.getId(), actor.getAssetTypeAsEnum());
                    break;
            }
        }

        void updateActor(@NonNull Actor actor) {
            getLog().v("Updating the actor: id = %s", actor.getId());

            switch (actor.getAssetTypeAsEnum()) {
                case IMAGE:
                    updateImageActor(actor);
                    break;
                default:
                    getLog().e("Unexpected asset type: actorId = %s, assetType = %s",
                               actor.getId(), actor.getAssetTypeAsEnum());
                    break;
            }
        }

        void removeActor(@NonNull String actorId) {
            getLog().v("Removing the actor: actorId = %s", actorId);

            renderer.removeActor(actorId);
        }

        void clearAllActors() {
            getLog().v("Clearing all actors.");

            renderer.clearAllActors();
        }

        void addImageActor(@NonNull Actor actor) {
            if (actor.getAssetId() == null) throw new IllegalArgumentException("actor.getAssetId() must be not null.");

            Disposable disposable = Single.<Uri>create(e -> {
                visionService.getUserAssetApi()
                             .getUserImageAssetFileUriById(actor.getAssetId(), e::onSuccess, e::onError);
            }).subscribe(uri -> {
                final ImageActorModel model = new ImageActorModel(actor);
                renderer.addActorModel(model);

                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        getLog().d("onBitmapLoaded: actorId = %s, uri = %s", model.actor.getId(), uri);

                        model.bitmap = bitmap;

                        renderer.updateActorModel(model);

                        // Dereference.
                        targetMap.remove(this);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        getLog().e("onBitmapFailed: actorId = %s, uri = %s", model.actor.getId(), uri);

                        // TODO

                        // Dereference.
                        targetMap.remove(this);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };
                // Hold the strong reference to the Target instance,
                // because Piccaso uses it as a weak reference.
                targetMap.add(target);

                picasso.load(uri)
                       .into(target);
            }, e -> {
                getLog().e("Failed.", e);
            });
            compositeDisposable.add(disposable);
        }

        void updateImageActor(@NonNull Actor actor) {
            ImageActorModel model = new ImageActorModel(actor);
            renderer.updateActorModel(model);
        }
    }
}
