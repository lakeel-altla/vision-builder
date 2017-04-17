package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaByPlaceListPresenter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaDescriptionByAreaListPresenter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaFindPresenter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaModePresenter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsContainerPresenter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsListPresenter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsPresenter;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class AreaSettingsContainerFragment
        extends AbstractFragment<AreaSettingsContainerPresenter.View, AreaSettingsContainerPresenter>
        implements AreaSettingsContainerPresenter.View,
                   AreaSettingsPresenter.ParentView,
                   AreaSettingsListPresenter.ParentView,
                   AreaModePresenter.ParentView,
                   AreaFindPresenter.ParentView,
                   AreaByPlaceListPresenter.ParentView,
                   AreaDescriptionByAreaListPresenter.ParentView {

    @Inject
    AreaSettingsContainerPresenter presenter;

    @Inject
    AppCompatActivity activity;

    @Inject
    GoogleApiClient googleApiClient;

    private InteractionListener interactionListener;

    @NonNull
    public static AreaSettingsContainerFragment newInstance() {
        return new AreaSettingsContainerFragment();
    }

    @Override
    protected AreaSettingsContainerPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaSettingsContainerPresenter.View getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
        interactionListener = InteractionListener.class.cast(getParentFragment());
    }

    @Override
    protected void onDetachOverride() {
        super.onDetachOverride();

        interactionListener = null;
    }

    @Nullable
    @Override
    protected View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                    @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_area_settings_container, container, false);
    }

    @Override
    public void onCloseAreaSettingsView() {
        interactionListener.onUpdateAreaSettingsVisible(false);
        interactionListener.onUpdateMainMenuVisible(true);
    }

    @Override
    public void onShowAreaSettingsHistoryView() {
        replaceFragmentAndAddToBackStack(AreaSettingsListFragment.newInstance());
    }

    @Override
    public void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                       @NonNull Area area,
                                       @NonNull AreaDescription areaDescription) {
        AreaSettingsFragment fragment = findAreaSettingsFragment();
        if (fragment != null) {
            fragment.onAreaSettingsSelected(areaSettings, area, areaDescription);
        }
    }

    @Override
    public void onCloseAreaSettingsListView() {
        backFragment();
    }

    @Override
    public void onShowAreaModeView(@NonNull Scope scope) {
        replaceFragmentAndAddToBackStack(AreaModeFragment.newInstance(scope));
    }

    @Override
    public void onCloseAreaModeView() {
        backFragment();
    }

    @Override
    public void onShowAreaFindView(@NonNull Scope scope) {
        replaceFragmentAndAddToBackStack(AreaFindFragment.newInstance(scope));
    }

    @Override
    public void onShowAreaDescriptionByAreaListView(@NonNull Scope scope, @NonNull Area area) {
        replaceFragmentAndAddToBackStack(AreaDescriptionByAreaListFragment.newInstance(scope, area));
    }

    @Override
    public void onShowAreaSettingsView(@NonNull Scope scope) {
        replaceFragment(AreaSettingsFragment.newInstance(scope));
    }

    @Override
    public void onShowAreaByPlaceListView(@NonNull Scope scope, @NonNull Place place) {
        replaceFragmentAndAddToBackStack(AreaByPlaceListFragment.newInstance(scope, place));
    }

    @Override
    public void onAreaModeSelected(@NonNull Scope scope) {
        AreaSettingsFragment fragment = findAreaSettingsFragment();
        if (fragment != null) {
            fragment.onAreaModeSelected(scope);
        }
    }

    @Override
    public void onCloseAreaFindView() {
        backFragment();
    }

    @Override
    public void onAreaSelected(@NonNull Area area) {
        AreaSettingsFragment fragment = findAreaSettingsFragment();
        if (fragment != null) {
            fragment.onAreaSelected(area);
        }
    }

    @Override
    public void onBackToAreaFindView() {
        backFragment();
    }

    @Override
    public void onCloseAreaByPlaceListView() {
        getChildFragmentManager().popBackStack(AreaFindFragment.class.getName(),
                                               FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onAreaDescriptionSelected(@NonNull AreaDescription areaDescription) {
        AreaSettingsFragment fragment = findAreaSettingsFragment();
        if (fragment != null) {
            fragment.onAreaDescriptionSelected(areaDescription);
        }
    }

    @Override
    public void onUpdateArView(@NonNull String areaSettingsId) {
        interactionListener.onUpdateArView(areaSettingsId);
    }

    @Override
    public void onCloseAreaDescriptionByAreaListView() {
        backFragment();
    }

    @Nullable
    private AreaSettingsFragment findAreaSettingsFragment() {
        return (AreaSettingsFragment) findFragment(AreaSettingsFragment.class);
    }

    @Nullable
    private Fragment findFragment(Class<? extends Fragment> clazz) {
        return getChildFragmentManager().findFragmentByTag(clazz.getName());
    }

    private void replaceFragmentAndAddToBackStack(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                                 .addToBackStack(fragment.getClass().getName())
                                 .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                 .commit();
    }

    private void replaceFragment(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                                 .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                                 .commit();
    }

    private void backFragment() {
        if (0 < getChildFragmentManager().getBackStackEntryCount()) {
            getChildFragmentManager().popBackStack();
        }
    }

    public interface InteractionListener {

        void onUpdateAreaSettingsVisible(boolean visible);

        void onUpdateMainMenuVisible(boolean visible);

        void onUpdateArView(@NonNull String areaSettingsId);
    }
}
