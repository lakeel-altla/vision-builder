package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsContainerPresenter;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class AreaSettingsContainerFragment
        extends AbstractFragment<AreaSettingsContainerPresenter.View, AreaSettingsContainerPresenter>
        implements AreaSettingsContainerPresenter.View {

    @Inject
    AreaSettingsContainerPresenter presenter;

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
    }

    @Nullable
    @Override
    protected View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                    @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_area_settings_container, container, false);
    }

    @Override
    protected void onCreateViewOverride(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onCreateViewOverride(view, savedInstanceState);

        if (savedInstanceState == null) {
            addFragment(AreaSettingsFragment.newInstance(Scope.USER));
        }
    }

    @Override
    public void showAreaSettingsListView() {
        addFragment(AreaSettingsListFragment.newInstance());
    }

    @Override
    public void showAreaModeView(@NonNull Scope scope) {
        addFragment(AreaModeFragment.newInstance(scope));
    }

    @Override
    public void showAreaFindView(@NonNull Scope scope) {
        addFragment(AreaFindFragment.newInstance(scope));
    }

    @Override
    public void showAreaDescriptionByAreaListView(@NonNull Scope scope, @NonNull Area area) {
        addFragment(AreaDescriptionByAreaListFragment.newInstance(scope, area));
    }

    @Override
    public void showAreaByPlaceListView(@NonNull Scope scope, @NonNull Place place) {
        addFragment(AreaByPlaceListFragment.newInstance(scope, place));
    }

    @Override
    public void backView() {
        backFragment();
    }

    @Override
    public void closeAreaByPlaceListView() {
        getChildFragmentManager().popBackStack(AreaFindFragment.class.getName(),
                                               FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void addFragment(@NonNull Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        Fragment topFragment = getTopFragment();
        if (topFragment != null) transaction.hide(topFragment);

        transaction.addToBackStack(fragment.getClass().getName())
                   .add(R.id.fragment_container, fragment, fragment.getClass().getName())
                   .commit();
    }

    private void backFragment() {
        if (0 < getChildFragmentManager().getBackStackEntryCount()) {
            getChildFragmentManager().popBackStack();
        }
    }

    @Nullable
    private Fragment getTopFragment() {
        FragmentManager manager = getChildFragmentManager();

        int index = manager.getBackStackEntryCount() - 1;
        if (index < 0) {
            return null;
        } else {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(index);
            return manager.findFragmentByTag(entry.getName());
        }
    }
}
