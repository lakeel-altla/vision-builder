package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.google.atap.tangoservice.Tango;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.tango.TangoWrapper;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.helper.ObservableHelper;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ArFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SignInFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.TangoPermissionFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class MainActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   FirebaseAuth.AuthStateListener,
                   TangoWrapper.OnTangoReadyListener,
                   SignInFragment.InteractionListener,
                   TangoPermissionFragment.InteractionListener,
                   ArFragment.InteractionListener {

    private static final Log LOG = LogFactory.getLog(MainActivity.class);

    @Inject
    VisionService visionService;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ActivityComponent activityComponent;

    private Disposable observeConnectionDisposable;

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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            showSignInView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseAuth.getInstance().removeAuthStateListener(this);

        // Unsubscribe the connection.
        if (observeConnectionDisposable != null) {
            observeConnectionDisposable.dispose();
            observeConnectionDisposable = null;
        }

        compositeDisposable.clear();
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            // Subscribe the connection.
            if (observeConnectionDisposable == null) {
                observeConnectionDisposable = ObservableHelper
                        .usingData(() -> visionService.getFirebaseConnectionApi().observeConnection())
                        .doOnNext(connected -> LOG
                                .i("The user device connection state changed: connected = %b", connected))
                        .flatMapCompletable(connected -> {
                            return Completable.create(e -> {
                                if (connected) {
                                    visionService.getUserDeviceConnectionApi()
                                                 .markUserDeviceConnectionAsOnline(aVoid -> {
                                                     e.onComplete();
                                                 }, e::onError);
                                } else {
                                    e.onComplete();
                                }
                            });
                        }).subscribe();
            }
        } else {
            // Unsubscribe the connection.
            if (observeConnectionDisposable != null) {
                observeConnectionDisposable.dispose();
                observeConnectionDisposable = null;
            }
        }
    }

    @Override
    public void onTangoReady(Tango tango) {
        LOG.d("Tango is ready.");
    }

    @Override
    public void onCloseSignInView() {
        toolbar.setVisibility(View.INVISIBLE);

        replaceFragment(TangoPermissionFragment.newInstance());
    }

    @Override
    public void onCloseTangoPermissionView() {
        toolbar.setVisibility(View.VISIBLE);

        replaceFragment(ArFragment.newInstance());
    }

    @Override
    public void onUpdateActionBarVisible(boolean visible) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (visible) {
                actionBar.show();
            } else {
                actionBar.hide();
            }
        }
    }

    @Override
    public void onShowSignInView() {
        showSignInView();
    }

    private void showSignInView() {
        toolbar.setVisibility(View.INVISIBLE);

        replaceFragment(SignInFragment.newInstance());
    }

    private void replaceFragmentAndAddToBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .addToBackStack(null)
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }
}
