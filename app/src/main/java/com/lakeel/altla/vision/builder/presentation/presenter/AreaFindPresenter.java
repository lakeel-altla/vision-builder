package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaByPlaceListViewEvent;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import javax.inject.Inject;

public final class AreaFindPresenter extends BasePresenter<AreaFindPresenter.View> {

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    public final RelayCommand commandShowPlacePicker = new RelayCommand(this::showPlacePicker);

    @Inject
    public AreaFindPresenter() {
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(ActionBarVisibleEvent.VISIBLE);
        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_area_find_view)));
        eventBus.post(HomeAsUpVisibleEvent.VISIBLE);
        eventBus.post(new HomeAsUpIndicatorEvent(resources.getDrawable(R.drawable.ic_arrow_back_white_24dp)));
    }

    public void onPlacePicked(@NonNull Place place) {
        eventBus.post(new ShowAreaByPlaceListViewEvent(place));
    }

    public void onShowPlacePickerFailed(@NonNull Exception e) {
        SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
    }

    private void showPlacePicker() {
        getView().onShowPlacePicker();
    }

    public interface View {

        void onShowPlacePicker();
    }
}
