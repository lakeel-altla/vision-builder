package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaFindPresenter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class AreaFindFragment extends AbstractFragment<AreaFindPresenter.View, AreaFindPresenter>
        implements AreaFindPresenter.View {

    private static final int REQUEST_CODE_PLACE_PICKER = 1;

    @Inject
    AreaFindPresenter presenter;

    @Inject
    AppCompatActivity activity;

    @Inject
    GoogleApiClient googleApiClient;

    private Place pickedPlace;

    @NonNull
    public static AreaFindFragment newInstance() {
        return new AreaFindFragment();
    }

    @Override
    protected AreaFindPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaFindPresenter.View getViewInterface() {
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
        return inflater.inflate(R.layout.fragment_area_find, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ViewBindingFactory factory = new ViewBindingFactory(view);
        factory.create(R.id.button_place_picker, "onClick", presenter.commandShowPlacePicker).bind();
    }

    @Override
    protected void onResumeOverride() {
        super.onResumeOverride();

        if (pickedPlace != null) {
            presenter.onPlacePicked(pickedPlace);
            pickedPlace = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PLACE_PICKER) {
            // TODO: renable the action to pick a place.

            if (resultCode == Activity.RESULT_OK) {
                pickedPlace = PlacePicker.getPlace(getContext(), data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onShowPlacePicker() {
        if (googleApiClient.isConnected()) {
            // TODO: disable the action to pick a place.
            try {
                Intent intent = new PlacePicker.IntentBuilder().build(activity);
                startActivityForResult(intent, REQUEST_CODE_PLACE_PICKER);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                presenter.onShowPlacePickerFailed(e);
            }
        }
    }
}
