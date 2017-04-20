package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.IntProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public final class AreaModePresenter extends BasePresenter<AreaModePresenter.View> {

    private static final String ARG_SCOPE = "scrope";

    private static final String STATE_SCOPE = "scope";

    @Inject
    EventBus eventBus;

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
        eventBus.post(new AreaModeSelectedEvent(scope));
        close();
    }

    private void close() {
        eventBus.post(CloseViewEvent.INSTANCE);
    }

    public interface View {

    }

    public final class AreaModeSelectedEvent {

        @NonNull
        public final Scope scope;

        private AreaModeSelectedEvent(@NonNull Scope scope) {
            this.scope = scope;
        }
    }

    public static final class CloseViewEvent {

        private static final CloseViewEvent INSTANCE = new CloseViewEvent();

        private CloseViewEvent() {
        }
    }
}
