package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.IntProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.view.AreaModeView;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.parceler.Parcels;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public final class AreaModePresenter extends BasePresenter<AreaModeView> {

    private static final String ARG_SCOPE = "scrope";

    private static final String STATE_SCOPE = "scope";

    public final IntProperty propertyChckedButton = new IntProperty(R.id.radio_button_public);

    public final RelayCommand commandSelect = new RelayCommand(this::select);

    public final RelayCommand commandClose = new RelayCommand(this::close);

    private Scope scope;

    @Inject
    public AreaModePresenter() {
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

        Scope initialScope = Parcels.unwrap(arguments.getParcelable(ARG_SCOPE));
        if (initialScope == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_SCOPE));
        }

        Scope scope = null;
        if (savedInstanceState != null) scope = Parcels.unwrap(savedInstanceState.getParcelable(STATE_SCOPE));
        if (scope == null) scope = initialScope;

        propertyChckedButton.addOnValueChangedListener(sender -> this.scope = resolveScope(propertyChckedButton.get()));
        propertyChckedButton.set(resolveCheckedId(scope));
    }

    @IdRes
    private static int resolveCheckedId(@NonNull Scope scope) {
        return (scope == Scope.PUBLIC) ? R.id.radio_button_public : R.id.radio_button_user;
    }

    @NonNull
    private static Scope resolveScope(@IdRes int checkedId) {
        return (checkedId == R.id.radio_button_public) ? Scope.PUBLIC : Scope.USER;
    }

    private void select() {
        getView().onAreaModeSelected(scope);
        getView().onCloseView();
    }

    private void close() {
        getView().onCloseView();
    }
}
