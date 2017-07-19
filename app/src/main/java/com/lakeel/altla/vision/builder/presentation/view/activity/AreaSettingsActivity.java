package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaListByPlaceFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaDescriptionListByAreaFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaFindFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaModeFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsListFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

public final class AreaSettingsActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   AreaSettingsFragment.FragmentContext,
                   AreaModeFragment.FragmentContext,
                   AreaFindFragment.FragmentContext,
                   AreaListByPlaceFragment.FragmentContext,
                   AreaDescriptionListByAreaFragment.FragmentContext,
                   AreaSettingsListFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(AreaSettingsActivity.class);

    private ActivityComponent activityComponent;

    private GoogleApiClient googleApiClient;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_settings);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            LOG.w("ActionBar is null.");
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionResult -> {
                    LOG.e("Google API connection error occured: %s", connectionResult);
                    Toast.makeText(this, R.string.toast_google_api_client_connection_failed, Toast.LENGTH_LONG).show();
                })
                .addApi(Places.GEO_DATA_API)
                .build();

        if (savedInstanceState == null) {
            replaceFragment(AreaSettingsFragment.newInstance());
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
    public void onBackPressed() {
        backView();
    }

    @Override
    public void setHomeAsUpIndicator(@DrawableRes int resId) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(resId);
        } else {
            LOG.w("ActionBar is null.");
        }
    }

    @Override
    public void backView() {
        if (0 < getSupportFragmentManager().getBackStackEntryCount()) {
            getSupportFragmentManager().popBackStack();
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    @Override
    public void closeAreaByPlaceListView() {
        getSupportFragmentManager().popBackStack(AreaFindFragment.class.getName(),
                                                 FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void showAreaModeView() {
        replaceFragmentAndAddToBackStack(AreaModeFragment.newInstance());
    }

    @Override
    public void showAreaFindView() {
        replaceFragmentAndAddToBackStack(AreaFindFragment.newInstance());
    }

    @Override
    public void showAreaDescriptionByAreaListView() {
        replaceFragmentAndAddToBackStack(AreaDescriptionListByAreaFragment.newInstance());
    }

    @Override
    public void showAreaSettingsListView() {
        replaceFragmentAndAddToBackStack(AreaSettingsListFragment.newInstance());
    }

    @Override
    public void showAreaByPlaceListView() {
        replaceFragmentAndAddToBackStack(AreaListByPlaceFragment.newInstance());
    }

    @Override
    public ActivityComponent getActivityComponent() {
        return activityComponent;
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
