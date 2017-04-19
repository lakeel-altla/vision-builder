package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import javax.inject.Inject;

public final class AreaFindPresenter extends BasePresenter<AreaFindPresenter.View> {

    private static final String ARG_SCOPE = "scope";

    private Scope scope;

    public final RelayCommand commandShowPlacePicker = new RelayCommand(this::showPlacePicker);

    public final RelayCommand commandClose = new RelayCommand(this::close);

    @Inject
    public AreaFindPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Scope scope) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SCOPE, Parcels.wrap(scope));
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        scope = Parcels.unwrap(arguments.getParcelable(ARG_SCOPE));
        if (scope == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_SCOPE));
        }
    }

    public void onPlacePicked(@NonNull Place place) {
        EventBus.getDefault().post(new ShowAreaByPlaceListViewEvent(scope, place));
    }

    public void onShowPlacePickerFailed(@NonNull Exception e) {
        getView().onSnackbar(R.string.snackbar_failed);
    }

    private void showPlacePicker() {
        getView().onShowPlacePicker();
    }

    private void close() {
        EventBus.getDefault().post(CloseViewEvent.INSTANCE);
    }

    public interface View {

        void onShowPlacePicker();

        void onSnackbar(@StringRes int resId);
    }

    public final class ShowAreaByPlaceListViewEvent {

        @NonNull
        public final Scope scope;

        @NonNull
        public final Place place;

        public ShowAreaByPlaceListViewEvent(@NonNull Scope scope, @NonNull Place place) {
            this.scope = scope;
            this.place = place;
        }
    }

    public static final class CloseViewEvent {

        private static final CloseViewEvent INSTANCE = new CloseViewEvent();

        private CloseViewEvent() {
        }
    }
}
