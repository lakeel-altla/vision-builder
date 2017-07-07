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
import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.graphics.ArGraphics;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.AreaSettings;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ArActivity extends AndroidApplication implements ArGraphics.Listener {

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

    @BindView(R.id.view_group_view_mode_menu)
    ViewGroup viewGroupViewModeMenu;

    @BindView(R.id.view_group_edit_mode_menu)
    ViewGroup viewGroupEditModeMenu;

    @BindView(R.id.view_actor)
    View viewActor;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final ActorView actorView = new ActorView();

    private ActivityComponent activityComponent;

    private ArGraphics arGraphics;

    private Tango tango;

    private boolean tangoSupportInitialized;

    private Actor selectedActor;

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
        actorView.onCreateView(viewActor);

        //
        // Set the initial state of views.
        //
        viewGroupEditModeMenu.setVisibility(View.GONE);
        viewActor.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

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

                        runOnUiThread(() -> {
                            // TODO
//                    propertySwitchToEditModeVisible.set(true);
                        });

                        Disposable disposable = arModel
                                .loadActors()
                                .subscribe(actor -> {
                                    LOG.d("Loaded an actor: id = %s", actor.getId());
                                    addActor(actor);
                                }, e -> {
                                    LOG.e("Failed.", e);
                                    Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show();
                                });
                        compositeDisposable.add(disposable);
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
    protected void onStop() {
        super.onStop();

        compositeDisposable.clear();
    }

    @Override
    public void onActorSelected(@Nullable Actor actor) {
        runOnUiThread(() -> {
            selectedActor = actor;
            actorView.setActor(selectedActor);
        });
    }

    @OnClick(R.id.image_button_show_area_settings)
    void onClickShowAreaSettings() {
        final Intent intent = AreaSettingsActivity.createIntent(this);
        startActivity(intent);
    }

    @OnClick(R.id.image_button_show_settings)
    void onClickShowSettings() {
        final Intent intent = SettingsActivity.createIntent(this);
        startActivity(intent);
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
            visionService.getUserAssetApi()
                         .downloadImageAssetFile(actor.getAssetId(), e::onSuccess, e::onError, null);
        }).subscribe(file -> {
            Gdx.app.postRunnable(() -> {
                arGraphics.addImageActor(actor, file);
            });
        }, e -> {
            LOG.e("Failed.", e);
        });
        compositeDisposable.add(disposable);
    }

    class ActorView {

        View view;

        @BindView(R.id.text_view_name)
        TextView name;

        ActorView() {
        }

        void onCreateView(@NonNull View view) {
            this.view = view;

            ButterKnife.bind(this, view);
        }

        void setActor(@Nullable Actor actor) {
            name.setText(actor == null ? null : actor.getName());
            view.setVisibility(actor == null ? View.GONE : View.VISIBLE);
        }

        @OnClick(R.id.image_button_close)
        void onClickClose() {
            view.setVisibility(View.GONE);
        }
    }
}
