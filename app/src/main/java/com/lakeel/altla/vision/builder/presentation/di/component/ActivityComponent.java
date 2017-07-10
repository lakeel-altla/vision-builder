package com.lakeel.altla.vision.builder.presentation.di.component;

import com.lakeel.altla.vision.builder.presentation.di.ActivityScope;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.view.activity.ArActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.AreaSettingsActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.SettingsActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.SignInActivity;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaByPlaceListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaDescriptionByAreaListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaFindFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaModeFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ImageAssetListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SettingsFragment;

import dagger.Subcomponent;

/**
 * Defines the dagger component that manages objects per activity.
 */
@ActivityScope
@Subcomponent(modules = { ActivityModule.class })
public interface ActivityComponent {

    void inject(SignInActivity activity);

    void inject(ArActivity activity);

    void inject(SettingsActivity activity);

    void inject(AreaSettingsActivity activity);

    void inject(AreaByPlaceListFragment fragment);

    void inject(AreaDescriptionByAreaListFragment fragment);

    void inject(AreaSettingsFragment fragment);

    void inject(AreaSettingsListFragment fragment);

    void inject(AreaModeFragment fragment);

    void inject(AreaFindFragment fragment);

    void inject(SettingsFragment fragment);

    void inject(ImageAssetListFragment fragment);
}
