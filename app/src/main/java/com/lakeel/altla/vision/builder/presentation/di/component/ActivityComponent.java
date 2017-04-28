package com.lakeel.altla.vision.builder.presentation.di.component;

import com.lakeel.altla.vision.builder.presentation.di.ActivityScope;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.di.module.GoogleApiModule;
import com.lakeel.altla.vision.builder.presentation.view.activity.MainActivity;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ActorEditFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ActorFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ArFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaByPlaceListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaDescriptionByAreaListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaFindFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaModeFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ConsoleFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ImageAssetListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SignInFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.TangoPermissionFragment;

import dagger.Subcomponent;

/**
 * Defines the dagger component that manages objects per activity.
 */
@ActivityScope
@Subcomponent(modules = { ActivityModule.class,
                          GoogleApiModule.class })
public interface ActivityComponent {

    void inject(MainActivity activity);

    void inject(SignInFragment fragment);

    void inject(TangoPermissionFragment fragment);

    void inject(AreaByPlaceListFragment fragment);

    void inject(AreaDescriptionByAreaListFragment fragment);

    void inject(ArFragment fragment);

    void inject(AreaSettingsFragment fragment);

    void inject(AreaSettingsListFragment fragment);

    void inject(AreaModeFragment fragment);

    void inject(AreaFindFragment fragment);

    void inject(SettingsFragment fragment);

    void inject(ActorFragment fragment);

    void inject(ActorEditFragment fragment);

    void inject(ImageAssetListFragment fragment);

    void inject(ConsoleFragment fragment);
}
