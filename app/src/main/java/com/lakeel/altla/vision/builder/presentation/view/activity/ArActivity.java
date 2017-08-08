package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.graphics.ArGraphics;
import com.lakeel.altla.vision.builder.presentation.helper.TangoMesher;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.builder.presentation.model.Axis;
import com.lakeel.altla.vision.builder.presentation.view.pane.ActorEditMenuPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ActorMetadataPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.DebugMenuPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.EditModelMenuPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ImageAssetListPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.PaneGroup;
import com.lakeel.altla.vision.builder.presentation.view.pane.PaneLifecycle;
import com.lakeel.altla.vision.builder.presentation.view.pane.TriggerShapeListPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ViewModeMenuPane;
import com.lakeel.altla.vision.helper.TypedQuery;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Asset;
import com.lakeel.altla.vision.model.Component;
import com.lakeel.altla.vision.model.GeometryComponent;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.model.MeshComponent;
import com.lakeel.altla.vision.model.ShapeComponent;
import com.projecttango.tangosupport.TangoSupport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ArActivity extends AndroidApplication
        implements ActivityScopeContext,
                   ArGraphics.Listener,
                   DebugMenuPane.PaneContext,
                   ViewModeMenuPane.PaneContext,
                   EditModelMenuPane.PaneContext,
                   ImageAssetListPane.PaneContext,
                   TriggerShapeListPane.PaneContext,
                   ActorEditMenuPane.PaneContext {

    private static final Log LOG = LogFactory.getLog(ArActivity.class);

    private static final List<TangoCoordinateFramePair> COORDINATE_FRAME_PAIRS;

    static {
        COORDINATE_FRAME_PAIRS = new ArrayList<>();

        COORDINATE_FRAME_PAIRS.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                                                                TangoPoseData.COORDINATE_FRAME_DEVICE));

        COORDINATE_FRAME_PAIRS.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                                                                TangoPoseData.COORDINATE_FRAME_DEVICE));

        COORDINATE_FRAME_PAIRS.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                                                                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
    }

    @Inject
    ArModel arModel;

    @BindView(R.id.view_top)
    ViewGroup viewTop;

    private final PaneLifecycle paneLifecycle = new PaneLifecycle();

    private final PaneGroup paneGroup = new PaneGroup();

    private DebugMenuPane debugMenuPane;

    private ViewModeMenuPane viewModeMenuPane;

    private EditModelMenuPane editModelMenuPane;

    private ImageAssetListPane imageAssetListPane;

    private TriggerShapeListPane triggerShapeListPane;

    private ActorEditMenuPane actorEditMenuPane;

    private ActorMetadataPane actorMetadataPane;

    private ActivityComponent activityComponent;

    private ArGraphics arGraphics;

    private Tango tango;

    private boolean tangoSupportInitialized;

    private TypedQuery<Actor> queryUserActors;

    private boolean editMode;

    private TangoMesher tangoMesher;

    @NonNull
    public static Intent createIntent(@NonNull Activity activity) {
        return new Intent(activity, ArActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // NOTE:
        //
        // Any injection must be done before super.onCreate()
        // because fragments are already attached to an activity when they are resumed or instant-run.
        activityComponent = MyApplication.getApplicationComponent(this)
                                         .activityComponent(new ActivityModule(this));
        activityComponent.inject(this);

        super.onCreate(savedInstanceState);

        //
        // Prepare the window for libGDX.
        //
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.activity_ar);

        //
        // Bind views.
        //
        ButterKnife.bind(this);

        //
        // Add the OpenGL view provided by libGDX.
        //
        arGraphics = new ArGraphics(getWindowManager().getDefaultDisplay(), this);
        final AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        final View view = initializeForView(arGraphics, configuration);
        viewTop.addView(view, 0);

        // Initialize sub views
        debugMenuPane = new DebugMenuPane(this);
        viewModeMenuPane = new ViewModeMenuPane(this);
        editModelMenuPane = new EditModelMenuPane(this);
        imageAssetListPane = new ImageAssetListPane(this);
        triggerShapeListPane = new TriggerShapeListPane(this);
        actorEditMenuPane = new ActorEditMenuPane(this);
        actorMetadataPane = new ActorMetadataPane(this);

        paneLifecycle.add(debugMenuPane);
        paneLifecycle.add(viewModeMenuPane);
        paneLifecycle.add(editModelMenuPane);
        paneLifecycle.add(imageAssetListPane);
        paneLifecycle.add(triggerShapeListPane);
        paneLifecycle.add(actorEditMenuPane);
        paneLifecycle.add(actorMetadataPane);

        paneGroup.add(viewModeMenuPane);
        paneGroup.add(editModelMenuPane);
        paneGroup.add(imageAssetListPane);
        paneGroup.add(triggerShapeListPane);
        paneGroup.add(actorEditMenuPane);

        viewModeMenuPane.setOnVisibleChangedListener(visible -> {
            // Always deselect an actor before when the view mode is active.
            arModel.setSelectedActor(null);

            if (!visible) {
                actorMetadataPane.hide();
            }
        });

        //
        // Set the initial state of views.
        //
        paneGroup.show(R.id.pane_view_mode_menu);
        actorMetadataPane.hide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        paneLifecycle.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        paneLifecycle.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        paneLifecycle.onResume();

        // Connect tango service.
        tango = new Tango(this, () -> {
            LOG.d("Tango ready.");

            // Synchronize against disconnecting while the service is being used in other threads.
            synchronized (ArActivity.this) {
                try {
                    if (!tangoSupportInitialized) {
                        TangoSupport.initialize();
                        tangoSupportInitialized = true;
                    }

                    final TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);

                    // NOTE:
                    // Low latency integration is necessary to achieve a precise alignment of
                    // virtual objects with the RBG image and produce a good AR effect.
                    config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
                    // Enable the color camera.
                    config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
                    // NOTE:
                    // Javadoc says, "LEARNINGMODE and loading AREADESCRIPTION cannot be used if drift correction is enabled."
//                    config.putBoolean(TangoConfig.KEY_BOOLEAN_DRIFT_CORRECTION, true);

                    // Depth information is needed for the 3d reconstruction.
                    config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
                    config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);

                    final AreaSettings areaSettings = arModel.getAreaSettings();
                    if (areaSettings != null && areaSettings.getAreaDescriptionId() != null) {
                        config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, areaSettings.getAreaDescriptionId());
                    }

                    tangoMesher = new TangoMesher(tango, tangoMeshes -> {
                        Gdx.app.postRunnable(() -> arGraphics.updateTangoMeshes(tangoMeshes));
                    });
                    tangoMesher.start();

                    tango.connect(config);
                    tango.connectListener(COORDINATE_FRAME_PAIRS, new Tango.TangoUpdateCallback() {
                        @Override
                        public void onFrameAvailable(final int cameraId) {
                            if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                                Gdx.app.postRunnable(arGraphics::onFrameAvailable);
                            }
                        }

                        @Override
                        public void onPointCloudAvailable(final TangoPointCloudData pointCloudData) {
                            if (tangoMesher != null) {
                                tangoMesher.onPointCloudAvailable(pointCloudData);
                            }
                        }
                    });

                    LOG.d("Tango connected.");

                    arGraphics.onTangoConnected(tango);

                    if (areaSettings != null) {
                        // TODO: for public scope.
                        // TODO: for other actor types.
                        queryUserActors = arModel.loadUserActors();
                        queryUserActors.addTypedChildEventListener(
                                new TypedQuery.TypedChildEventListener<Actor>() {
                                    @Override
                                    public void onChildAdded(@NonNull Actor actor, @Nullable String previousChildName) {
                                        addActor(actor);
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull Actor actor, String previousChildName) {
                                        // TODO
                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull Actor actor) {
                                        Gdx.app.postRunnable(() -> arGraphics.removeGeometryObjectsByActor(actor));
                                    }

                                    @Override
                                    public void onChildMoved(@NonNull Actor actor, String previousChildName) {
                                    }

                                    @Override
                                    public void onError(@NonNull Exception e) {
                                        LOG.e("Failed.", e);
                                    }
                                });
                    }
                } catch (TangoOutOfDateException e) {
                    LOG.e("Tango service outdated.", e);
                    Toast.makeText(this, R.string.toast_tango_out_of_date, Toast.LENGTH_SHORT).show();
                } catch (TangoException e) {
                    LOG.e("Unexpected tango error occurred.", e);
                    Toast.makeText(this, R.string.toast_tango_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        paneLifecycle.onPause();

        // Disconnect the tango service.
        synchronized (this) {
            if (tangoMesher != null) {
                tangoMesher.stop();
                tangoMesher.release();
                tangoMesher = null;
            }

            if (tango != null) {
                LOG.d("Disconnecting tango...");
                arGraphics.onTangoDisconnecting();

                try {
                    tango.disconnect();
                    tango = null;
                } catch (TangoException e) {
                    LOG.e("Unexpected tango error occurred.", e);
                    Toast.makeText(this, R.string.toast_tango_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void onActorObjectTouched(@Nullable Actor actor) {
        LOG.d("onActorObjectTouched");

        runOnUiThread(() -> {
            arModel.setSelectedActor(actor);
            if (editMode) {
                if (actor == null) {
                    paneGroup.show(R.id.pane_edit_mode_menu);
                } else {
                    paneGroup.show(R.id.pane_actor_edit_menu);
                }
                Gdx.app.postRunnable(() -> {
                    arGraphics.setActorAxesObjectVisible(actor != null);
                });
            } else {
                if (actor == null) {
                    actorMetadataPane.hide();
                } else {
                    actorMetadataPane.show();
                }
            }
        });
    }

    @Override
    public void onMeshActorCursorObjectTouched(@NonNull Asset asset, @NonNull Vector3 position,
                                               @NonNull Quaternion orientation, @NonNull Vector3 scale) {
        runOnUiThread(() -> arModel.saveMeshActor(asset, position, orientation, scale));
    }

    @Override
    public void onTriggerActorCursorObjectTouched(@NonNull Class<? extends ShapeComponent> clazz,
                                                  @NonNull Vector3 position, @NonNull Quaternion orientation,
                                                  @NonNull Vector3 scale) {
        runOnUiThread(() -> arModel.saveTriggerActor(clazz, position, orientation, scale));
    }

    @Override
    public void onActorChanged(@NonNull Actor actor) {
        runOnUiThread(() -> arModel.saveActor(actor));
    }

    @Override
    public void setDebugFrameBuffersVisible(boolean visible) {
        Gdx.app.postRunnable(() -> arGraphics.setDebugFrameBuffersVisible(visible));
    }

    @Override
    public void setDebugTangoMeshesVisible(boolean visible) {
        Gdx.app.postRunnable(() -> arGraphics.setDebugTangoMeshesVisible(visible));
    }

    @Override
    public void setDebugCamerePreviewVisible(boolean visible) {
        Gdx.app.postRunnable(() -> arGraphics.setDebugCameraPreviewVisible(visible));
    }

    @Override
    public void showViewModeMenuPane() {
        paneGroup.show(R.id.pane_view_mode_menu);
        editMode = false;
    }

    @Override
    public void showEditModeMenuPane() {
        paneGroup.show(R.id.pane_edit_mode_menu);
        editMode = true;
    }

    @Override
    public void showImageAssetListPane() {
        paneGroup.show(R.id.pane_image_asset_list);
    }

    @Override
    public void showTriggerListPane() {
        paneGroup.show(R.id.pane_trigger_shape_list);
    }

    @Override
    public void onImageAssetSelected(@Nullable ImageAsset asset) {
        if (asset == null) {
            Gdx.app.postRunnable(() -> arGraphics.removeMeshActorCursor());
        } else {
            arModel.getCachedAssetFile(asset.getId(), asset.getType(), file -> {
                Gdx.app.postRunnable(() -> arGraphics.addMeshActorCursor(asset, file));
            }, e -> {
                LOG.e("Failed.", e);
            }, null);
        }
    }

    @Override
    public void onTriggerShapeSelected(@Nullable Class<? extends ShapeComponent> clazz) {
        Gdx.app.postRunnable(() -> {
            if (clazz == null) {
                arGraphics.removeTriggerActorCursor();
            } else {
                arGraphics.addTriggerActorCursor(clazz);
            }
        });
    }

    @Override
    public void closeActorEditMenu() {
        paneGroup.show(R.id.pane_edit_mode_menu);
    }

    @Override
    public void showActorMetadataEditView() {
        final Intent intent = ActorMetadataEditActivity.createIntent(this);
        startActivity(intent);
    }

    @Override
    public void setSelectedActorLocked(boolean locked) {
        Gdx.app.postRunnable(() -> {
            arGraphics.setTouchedActorObjectLocked(locked);
        });
    }

    @Override
    public void setTranslationEnabled(boolean enabled, @Nullable Axis axis) {
        Gdx.app.postRunnable(() -> {
            arGraphics.setTranslationEnabled(enabled, axis);
        });
    }

    @Override
    public void setRotationEnabled(boolean enabled, @Nullable Axis axis) {
        Gdx.app.postRunnable(() -> {
            arGraphics.setRotationEnabled(enabled, axis);
        });
    }

    @Override
    public void setScaleEnabled(boolean enabled) {
        Gdx.app.postRunnable(() -> {
            arGraphics.setScaleEnabled(enabled);
        });
    }

    private void addActor(@NonNull Actor actor) {
        LOG.v("Adding an actor: id = %s", actor.getId());

        for (final Component component : actor.getComponents()) {
            if (component instanceof GeometryComponent) {
                addGeometryComponent(actor, (GeometryComponent) component);
            }
        }
    }

    private void addGeometryComponent(@NonNull Actor actor, @NonNull GeometryComponent component) {
        if (component instanceof MeshComponent) {
            addMeshComponent(actor, (MeshComponent) component);
        } else if (component instanceof ShapeComponent) {
            addShapeComponent(actor, (ShapeComponent) component);
        }
    }

    private void addMeshComponent(@NonNull Actor actor, @NonNull MeshComponent component) {
        final String assetId = component.getRequiredAssetId();
        final String assetType = component.getRequiredAssetType();

        if (ImageAsset.TYPE.equals(assetType)) {
            // TODO: Using something like a loader class.
            arModel.getCachedAssetFile(assetId, assetType, file -> {
                Gdx.app.postRunnable(() -> arGraphics.addGeometryObject(actor, component, file));
            }, e -> {
                LOG.e("Failed.", e);
            }, null);
        } else {
            throw new IllegalStateException("An unexpected asset type: " + assetType);
        }
    }

    private void addShapeComponent(@NonNull Actor actor, @NonNull ShapeComponent component) {
        Gdx.app.postRunnable(() -> arGraphics.addGeometryObject(actor, component));
    }
}
