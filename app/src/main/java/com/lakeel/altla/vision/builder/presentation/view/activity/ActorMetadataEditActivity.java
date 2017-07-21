package com.lakeel.altla.vision.builder.presentation.view.activity;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.app.MyApplication;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.di.component.ActivityComponent;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ActorMetadataEditFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class ActorMetadataEditActivity extends AppCompatActivity
        implements ActivityScopeContext,
                   ActorMetadataEditFragment.FragmentContext {

    private static final Log LOG = LogFactory.getLog(ActorMetadataEditActivity.class);

    private ActivityComponent activityComponent;

    @NonNull
    public static Intent createIntent(@NonNull Activity activity) {
        return new Intent(activity, ActorMetadataEditActivity.class);
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
        setContentView(R.layout.activity_actor_metadata_edit);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_actor_metadata_edit_view);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        } else {
            LOG.w("ActionBar is null.");
        }

        if (savedInstanceState == null) {
            replaceFragment(ActorMetadataEditFragment.newInstance());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
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
    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                   .commit();
    }
}
