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
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ClearBackStackEvent;
import com.lakeel.altla.vision.builder.presentation.event.CloseAreaByPlaceListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowArViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaByPlaceListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaDescriptionByAreaListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaFindViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaModeViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowSettingsViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowSignInViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowTangoPermissionViewEvent;
import com.lakeel.altla.vision.builder.presentation.helper.ObservableHelper;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ArFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaByPlaceListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaDescriptionByAreaListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaFindFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaModeFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SignInFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.TangoPermissionFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class MainActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   FirebaseAuth.AuthStateListener {

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

        eventBus.register(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

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
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();

        int top = manager.getBackStackEntryCount() - 1;
        if (0 <= top) {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(top);
            if (ArFragment.class.getName().equals(entry.getName())) {
                finish();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
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
        } else {
            LOG.w("ActionBar is null.");
        }
    }

    @Subscribe
    public void onEvent(@NonNull ActionBarTitleEvent event) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(event.title);
        } else {
            LOG.w("ActionBar is null.");
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
        } else {
            LOG.w("ActionBar is null.");
        }
    }

    @Subscribe
    public void onEvent(@NonNull HomeAsUpIndicatorEvent event) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(event.indicator);
        } else {
            LOG.w("ActionBar is null.");
        }
    }

    @Subscribe
    public void onEvent(@NonNull BackViewEvent event) {
        Fragment fragment = (Fragment) event.view;
        if (getSupportFragmentManager().findFragmentByTag(fragment.getTag()) != null) {
            backView();
        } else {
            LOG.w("No such view exists: tag = %s", fragment.getTag());
        }
    }

    @Subscribe
    public void onEvent(@NonNull ClearBackStackEvent event) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Subscribe
    public void onEvent(@NonNull ShowSignInViewEvent event) {
        replaceFragment(SignInFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowTangoPermissionViewEvent event) {
        replaceFragment(TangoPermissionFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowArViewEvent event) {
        replaceFragmentAndAddToBackStack(ArFragment.newInstance());
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
        replaceFragmentAndAddToBackStack(AreaByPlaceListFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowAreaDescriptionByAreaListViewEvent event) {
        replaceFragmentAndAddToBackStack(AreaDescriptionByAreaListFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull ShowSettingsViewEvent event) {
        replaceFragmentAndAddToBackStack(SettingsFragment.newInstance());
    }

    @Subscribe
    public void onEvent(@NonNull CloseAreaByPlaceListViewEvent event) {
        getSupportFragmentManager().popBackStack(AreaFindFragment.class.getName(),
                                                 FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void backView() {
        if (0 < getSupportFragmentManager().getBackStackEntryCount()) {
            getSupportFragmentManager().popBackStack();
        }
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

    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends Fragment> T findFragment(@NonNull Class<T> clazz) {
        return (T) getSupportFragmentManager().findFragmentByTag(clazz.getName());
    }
}
