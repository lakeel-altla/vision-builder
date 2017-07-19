package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.graphics.ArGraphics;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.builder.presentation.view.pane.ActorEditMenuPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ActorMetadataEditPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ActorMetadataPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.EditModelMenuPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ImageAssetListPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.PaneGroup;
import com.lakeel.altla.vision.builder.presentation.view.pane.PaneLifecycle;
import com.lakeel.altla.vision.builder.presentation.view.pane.ViewModeMenuPane;
import com.lakeel.altla.vision.helper.FirebaseQuery;
import com.lakeel.altla.vision.helper.OnFailureListener;
import com.lakeel.altla.vision.helper.OnSuccessListener;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Asset;
import com.lakeel.altla.vision.model.AssetType;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.model.Layer;
import com.lakeel.altla.vision.model.Scope;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ArActivity extends AndroidApplication
        implements ActivityScopeContext,
                   ArGraphics.Listener,
                   ViewModeMenuPane.PaneContext,
                   EditModelMenuPane.PageContext,
                   ImageAssetListPane.PageContext,
                   ActorEditMenuPane.PageContext {

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
    VisionService visionService;

    @Inject
    ArModel arModel;

    @BindView(R.id.view_top)
    ViewGroup viewTop;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final PaneLifecycle paneLifecycle = new PaneLifecycle();

    private final PaneGroup paneGroup = new PaneGroup();

    private ViewModeMenuPane viewModeMenuPane;

    private EditModelMenuPane editModelMenuPane;

    private ImageAssetListPane imageAssetListPane;

    private ActorEditMenuPane actorEditMenuPane;

    private ActorMetadataPane actorMetadataPane;

    private ActorMetadataEditPane actorMetadataEditPane;

    private ActivityComponent activityComponent;

    private ArGraphics arGraphics;

    private Tango tango;

    private boolean tangoSupportInitialized;

    private Actor selectedActor;

    private FirebaseQuery<Actor> queryUserActors;

    private boolean editMode;

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
        viewModeMenuPane = new ViewModeMenuPane(this);
        editModelMenuPane = new EditModelMenuPane(this);
        imageAssetListPane = new ImageAssetListPane(this);
        actorEditMenuPane = new ActorEditMenuPane(this);
        actorMetadataPane = new ActorMetadataPane(this);
        actorMetadataEditPane = new ActorMetadataEditPane(this);

        paneLifecycle.add(viewModeMenuPane);
        paneLifecycle.add(editModelMenuPane);
        paneLifecycle.add(imageAssetListPane);
        paneLifecycle.add(actorEditMenuPane);
        paneLifecycle.add(actorMetadataPane);
        paneLifecycle.add(actorMetadataEditPane);

        paneGroup.add(viewModeMenuPane);
        paneGroup.add(editModelMenuPane);
        paneGroup.add(imageAssetListPane);
        paneGroup.add(actorEditMenuPane);

        //
        // Set the initial state of views.
        //
        paneGroup.show(R.id.pane_view_mode_menu);
        actorMetadataPane.hide();
        actorMetadataEditPane.hide();
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
        compositeDisposable.clear();
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

                    final AreaSettings areaSettings = arModel.getAreaSettings();
                    if (areaSettings != null && areaSettings.getAreaDescriptionId() != null) {
                        config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, areaSettings.getAreaDescriptionId());
                    }

                    tango.connect(config);
                    tango.connectListener(COORDINATE_FRAME_PAIRS, new Tango.TangoUpdateCallback() {
                        @Override
                        public void onFrameAvailable(int cameraId) {
                            if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                                Gdx.app.postRunnable(arGraphics::onFrameAvailable);
                            }
                        }
                    });

                    LOG.d("Tango connected.");

                    arGraphics.onTangoConnected(tango);

                    if (areaSettings != null) {
                        if (areaSettings.getAreaId() == null) {
                            throw new IllegalStateException("Unknown areaId: null");
                        }

                        // TODO: for public scope
                        queryUserActors = arModel.loadUserActors();
                        queryUserActors.addListener(new FirebaseQuery.BaseChildListener<Actor>() {
                            @Override
                            public void onChildAdded(@NonNull Actor actor, @Nullable String previousChildName) {
                                addActor(actor);
                            }

                            @Override
                            public void onChildRemoved(@NonNull Actor actor) {
                                Gdx.app.postRunnable(() -> arGraphics.removeActor(actor));
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
    public void onActorSelected(@Nullable Actor actor) {
        runOnUiThread(() -> {
            selectedActor = actor;
            if (editMode) {
                if (selectedActor == null) {
                    paneGroup.show(R.id.pane_edit_mode_menu);
                } else {
                    paneGroup.show(R.id.pane_actor_edit_menu);
                    actorEditMenuPane.setActor(selectedActor);
                }
            } else {
                actorMetadataPane.setActor(selectedActor);
            }
        });
    }

    @Override
    public void onCursorSelected(@NonNull Asset asset, @NonNull AssetType assetType, @NonNull Vector3 position,
                                 @NonNull Quaternion rotation, @NonNull Vector3 scale) {
        runOnUiThread(() -> {
            final Actor actor = new Actor();
            actor.setUserId(CurrentUser.getInstance().getUserId());
            actor.setScopeAsEnum(Scope.USER);
            actor.setAreaId(arModel.getAreaSettings().getAreaId());
            actor.setAssetId(asset.getId());
            actor.setAssetTypeAsEnum(assetType);
            actor.setLayerAsEnum(Layer.NONCOMMERCIAL);
            actor.setName(asset.getName());
            actor.setPositionX(position.x);
            actor.setPositionY(position.y);
            actor.setPositionZ(position.z);
            actor.setOrientationX(rotation.x);
            actor.setOrientationY(rotation.y);
            actor.setOrientationZ(rotation.z);
            actor.setOrientationW(rotation.w);
            actor.setScaleX(scale.x);
            actor.setScaleY(scale.y);
            actor.setScaleZ(scale.z);

            // Save the actor.
            visionService.getUserActorApi()
                         .save(actor);

            // Add the actor into the scene.
            addActor(actor);
        });
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
    public void onImageAssetSelected(@Nullable ImageAsset asset) {
        LOG.d("onImageAssetSelected");

        if (asset == null) {
            Gdx.app.postRunnable(() -> {
                arGraphics.removeCursor();
            });
        } else {
            final Disposable disposable = Single.<File>create(e -> {
                ensureUserImageAssetCacheFile(asset.getId(), e::onSuccess, e::onError);
            }).subscribe(file -> {
                Gdx.app.postRunnable(() -> {
                    arGraphics.setImageAssetCursor(asset, file);
                });
            }, e -> {
                LOG.e("Failed.", e);
            });
            compositeDisposable.add(disposable);
        }
    }

    @Override
    public void closeActorEditMenu() {
        paneGroup.show(R.id.pane_edit_mode_menu);
        selectedActor = null;
    }

    @Override
    public void showActorMetadataEditPane() {
        actorMetadataEditPane.setActor(selectedActor);
    }

    //
    // NOTE:
    //
    // To keep a button pressed, call setPressed(true) and return true in onTouch event handlers
    // instead of an onClick ones.
    //

//    @OnTouch(R.id.button_translate)
//    boolean onTouchButtonTranslate(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            buttonTranslate.setPressed(true);
//            presenter.onTouchButtonTranslate();
//        }
//        return true;
//    }


    private void addActor(@NonNull Actor actor) {
        LOG.v("Adding the actor: id = %s", actor.getId());

        switch (actor.getAssetTypeAsEnum()) {
            case IMAGE:
                addImageActor(actor);
                break;
            default:
                LOG.e("Unexpected asset type: actorId = %s, assetType = %s", actor.getId(), actor.getAssetTypeAsEnum());
                break;
        }
    }

    private void addImageActor(@NonNull Actor actor) {
        if (actor.getAssetId() == null) throw new IllegalArgumentException("actor.getAssetId() must be not null.");

        final Disposable disposable = Single.<File>create(e -> {
            ensureUserImageAssetCacheFile(actor.getAssetId(), e::onSuccess, e::onError);
        }).subscribe(file -> {
            Gdx.app.postRunnable(() -> {
                arGraphics.addImageActor(actor, file);
            });
        }, e -> {
            LOG.e("Failed.", e);
        });
        compositeDisposable.add(disposable);
    }

    private void ensureUserImageAssetCacheFile(@NonNull String assetId,
                                               @NonNull OnSuccessListener<File> onSuccessListener,
                                               @NonNull OnFailureListener onFailureListener)
            throws IOException {
        final File file = visionService.getUserAssetApi().findUserImageAssetCacheFile(assetId);
        if (file == null) {
            // Download anc cache the file if it is not cached.
            visionService.getUserAssetApi()
                         .downloadUserImageAssetFile(assetId, onSuccessListener, onFailureListener, null);
        } else {
            // Stream the cache file if it exists.
            onSuccessListener.onSuccess(file);
        }
    }
}
