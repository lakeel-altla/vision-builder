package com.lakeel.altla.vision.builder.presentation.di.component;

import com.lakeel.altla.vision.builder.presentation.di.ActivityScope;
import com.lakeel.altla.vision.builder.presentation.di.module.ActivityModule;
import com.lakeel.altla.vision.builder.presentation.view.activity.ActorMetadataEditActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.ArActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.AreaSettingsActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.SettingsActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.SignInActivity;
import com.lakeel.altla.vision.builder.presentation.view.fragment.ActorMetadataEditFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaDescriptionListByAreaFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaFindFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaListByPlaceFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaModeFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.AreaSettingsListFragment;
import com.lakeel.altla.vision.builder.presentation.view.fragment.SettingsFragment;
import com.lakeel.altla.vision.builder.presentation.view.pane.ActorEditMenuPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ActorMetadataPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ImageAssetListPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.TriggerListPane;
import com.lakeel.altla.vision.builder.presentation.view.pane.ViewModeMenuPane;

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

    void inject(ActorMetadataEditActivity activity);

    void inject(AreaListByPlaceFragment fragment);

    void inject(AreaDescriptionListByAreaFragment fragment);

    void inject(AreaSettingsFragment fragment);

    void inject(AreaSettingsListFragment fragment);

    void inject(AreaModeFragment fragment);

    void inject(AreaFindFragment fragment);

    void inject(SettingsFragment fragment);

    void inject(ActorMetadataEditFragment fragment);

    void inject(ViewModeMenuPane pane);

    void inject(ImageAssetListPane pane);

    void inject(TriggerListPane pane);

    void inject(ActorEditMenuPane pane);

    void inject(ActorMetadataPane pane);
}
