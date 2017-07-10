package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class AreaFindFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(AreaFindFragment.class);

    private static final int REQUEST_CODE_PLACE_PICKER = 1;

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    @BindView(R.id.button_show_place_picker)
    Button buttonShowPlacePicker;

    private FragmentContext fragmentContext;

    private Place pickedPlace;

    @NonNull
    public static AreaFindFragment newInstance() {
        return new AreaFindFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
        fragmentContext = (FragmentContext) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_area_find, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(R.string.title_area_find_view);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (pickedPlace != null) {
            selectAreaSettingsModel.selectPlace(pickedPlace);
            fragmentContext.showAreaByPlaceListView();
            pickedPlace = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PLACE_PICKER) {
            buttonShowPlacePicker.setEnabled(true);
            if (resultCode == Activity.RESULT_OK) {
                pickedPlace = PlacePicker.getPlace(getContext(), data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.button_show_place_picker)
    void onClickShowPlacePicker() {
        buttonShowPlacePicker.setEnabled(false);
        try {
            final Intent intent = new PlacePicker.IntentBuilder().build(getActivity());
            startActivityForResult(intent, REQUEST_CODE_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            LOG.e("Failed.", e);
            Toast.makeText(getContext(), R.string.toast_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void showAreaByPlaceListView();
    }
}
