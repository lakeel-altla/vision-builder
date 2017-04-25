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
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaByPlaceListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaDescriptionByAreaListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaFindViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaModeViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsViewEvent;
import com.lakeel.altla.vision.builder.presentation.helper.ObservableHelper;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ArFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaByPlaceListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaDescriptionByAreaListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaFindFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaModeFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SignInFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.TangoPermissionFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

    @Inject
    EventBus eventBus;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(this);

        eventBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseAuth.getInstance().removeAuthStateListener(this);

        eventBus.unregister(this);

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
    public void onShowSignInView() {
        showSignInView();
    }

    @Subscribe
    public void onEvent(@NonNull ActionBarVisibleEvent event) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (event.visible) {
                actionBar.show();
            } else {
                actionBar.hide();
            }
        }
    }

    @Subscribe
    public void onEvent(@NonNull ActionBarTitleEvent event) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(event.title);
        }
    }

    @Subscribe
    public void onEvent(@NonNull InvalidateOptionsMenuEvent event) {
        invalidateOptionsMenu();
    }

    @Subscribe
    public void onEvent(@NonNull HomeAsUpVisibleEvent event) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(event.visible);
        }
    }

    @Subscribe
    public void onEvent(@NonNull HomeAsUpIndicatorEvent event) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(event.indicator);
        }
    }

    @Subscribe
    public void onEvent(@NonNull BackViewEvent event) {
        Fragment fragment = (Fragment) event.view;
        if (getSupportFragmentManager().findFragmentByTag(fragment.getTag()) != null) {
            backView();
        }
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaSettingsViewEvent event) {
        replaceFragmentAndAddToBackStack(AreaSettingsFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaSettingsListViewEvent event) {
        replaceFragmentAndAddToBackStack(AreaSettingsListFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaModeViewEvent event) {
        replaceFragmentAndAddToBackStack(AreaModeFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaFindViewEvent event) {
        replaceFragmentAndAddToBackStack(AreaFindFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaByPlaceListViewEvent event) {
        replaceFragmentAndAddToBackStack(AreaByPlaceListFragment.newInstance(event.place));
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaDescriptionByAreaListViewEvent event) {
        replaceFragmentAndAddToBackStack(AreaDescriptionByAreaListFragment.newInstance());
    }

    private void backView() {
        if (0 < getSupportFragmentManager().getBackStackEntryCount()) {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void showSignInView() {
        toolbar.setVisibility(View.INVISIBLE);

        replaceFragment(SignInFragment.newInstance());
    }

    private void addFragmentAndAddToBackStack(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment topFragment = getTopFragment();
        if (topFragment != null) transaction.hide(topFragment);

        transaction.addToBackStack(fragment.getClass().getName())
                   .add(R.id.fragment_container, fragment, fragment.getClass().getName())
                   .commit();
    }

    private void replaceFragmentAndAddToBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .addToBackStack(fragment.getClass().getName())
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }

    @Nullable
    private Fragment getTopFragment() {
        FragmentManager manager = getSupportFragmentManager();

        int index = manager.getBackStackEntryCount() - 1;
        if (index < 0) {
            return null;
        } else {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(index);
            return manager.findFragmentByTag(entry.getName());
        }
    }
}
