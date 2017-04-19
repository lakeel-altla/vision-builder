package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.support.annotation.NonNull;

import javax.inject.Inject;

public final class AreaSettingsContainerPresenter extends BasePresenter<AreaSettingsContainerPresenter.View> {

    @Inject
    public AreaSettingsContainerPresenter() {
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(@NonNull AreaSettingsPresenter.ShowAreaSettingsListViewEvent event) {
        getView().showAreaSettingsListView();
    }

    @Subscribe
    public void onEvent(@NonNull AreaSettingsPresenter.ShowAreaModeViewEvent event) {
        getView().showAreaModeView(event.scope);
    }

    @Subscribe
    public void onEvent(@NonNull AreaSettingsPresenter.ShowAreaFindViewEvent event) {
        getView().showAreaFindView(event.scope);
    }

    @Subscribe
    public void onEvent(@NonNull AreaSettingsPresenter.ShowAreaDescriptionByAreaListViewEvent event) {
        getView().showAreaDescriptionByAreaListView(event.scope, event.area);
    }

    @Subscribe
    public void onEvent(@NonNull AreaFindPresenter.ShowAreaByPlaceListViewEvent event) {
        getView().showAreaByPlaceListView(event.scope, event.place);
    }

    @Subscribe
    public void onEvent(@NonNull AreaByPlaceListPresenter.BackViewEvent event) {
        getView().backView();
    }

    @Subscribe
    public void onEvent(@NonNull AreaSettingsListPresenter.CloseViewEvent event) {
        getView().backView();
    }

    @Subscribe
    public void onEvent(@NonNull AreaModePresenter.CloseViewEvent event) {
        getView().backView();
    }

    @Subscribe
    public void onEvent(@NonNull AreaFindPresenter.CloseViewEvent event) {
        getView().backView();
    }

    @Subscribe
    public void onEvent(@NonNull AreaByPlaceListPresenter.CloseViewEvent event) {
        getView().closeAreaByPlaceListView();
    }

    @Subscribe
    public void onEvent(@NonNull AreaDescriptionByAreaListPresenter.CloseViewEvent event) {
        getView().backView();
    }

    public interface View {

        void showAreaSettingsListView();

        void showAreaModeView(@NonNull Scope scope);

        void showAreaFindView(@NonNull Scope scope);

        void showAreaDescriptionByAreaListView(@NonNull Scope scope, @NonNull Area area);

        void showAreaByPlaceListView(@NonNull Scope scope, @NonNull Place place);

        void backView();

        void closeAreaByPlaceListView();
    }
}
