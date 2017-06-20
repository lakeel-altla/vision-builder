package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.event.ShowArViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowTangoPermissionViewEvent;
import com.lakeel.altla.vision.builder.presentation.helper.ObservableHelper;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SignInFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.TangoPermissionFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class SignInActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   FirebaseAuth.AuthStateListener {

    private static final Log LOG = LogFactory.getLog(SignInActivity.class);

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ActivityComponent activityComponent;

    private Disposable observeConnectionDisposable;

    @NonNull
    public static Intent createIntent(@NonNull Activity activity) {
        return new Intent(activity, SignInActivity.class);
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

        eventBus.register(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            LOG.w("ActionBar is null.");
        }

        if (savedInstanceState == null) {
            replaceFragment(SignInFragment.newInstance());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        eventBus.unregister(this);
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

    @Subscribe
    public void onEvent(@NonNull ShowTangoPermissionViewEvent event) {
        replaceFragment(TangoPermissionFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowArViewEvent event) {
        Intent intent = ArActivity.createIntent(this);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }
}
