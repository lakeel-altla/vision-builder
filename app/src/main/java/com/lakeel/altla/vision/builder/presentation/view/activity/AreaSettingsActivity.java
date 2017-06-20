package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.CloseAreaByPlaceListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaByPlaceListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaDescriptionByAreaListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaFindViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaModeViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsListViewEvent;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaByPlaceListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaDescriptionByAreaListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaFindFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaModeFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsListFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import javax.inject.Inject;

public final class AreaSettingsActivity extends AppCompatActivity
        implements ActivityScopeContext {

    private static final Log LOG = LogFactory.getLog(AreaSettingsActivity.class);

    @Inject
    EventBus eventBus;

    private ActivityComponent activityComponent;

    @NonNull
    public static Intent createIntent(@NonNull Activity activity) {
        return new Intent(activity, AreaSettingsActivity.class);
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
        setContentView(R.layout.activity_area_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            LOG.w("ActionBar is null.");
        }

        if (savedInstanceState == null) {
            replaceFragment(AreaSettingsFragment.newInstance());
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
    public void onBackPressed() {
        backView();
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Subscribe
    public void onEvent(@NonNull BackViewEvent event) {
        backView();
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
    public void onEvent(@NonNull CloseAreaByPlaceListViewEvent event) {
        getSupportFragmentManager().popBackStack(AreaFindFragment.class.getName(),
                                                 FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void backView() {
        if (0 < getSupportFragmentManager().getBackStackEntryCount()) {
            getSupportFragmentManager().popBackStack();
        } else {
            NavUtils.navigateUpFromSameTask(this);
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
}
