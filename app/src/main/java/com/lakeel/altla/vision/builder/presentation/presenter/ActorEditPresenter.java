package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.property.BooleanProperty;
import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public final class ActorEditPresenter extends BasePresenter<ActorEditPresenter.View> {

    private static final String ARG_ACTOR = "actor";

    private static final String STATE_ACTOR = "actor";

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    public final StringProperty propertyName = new StringProperty();

    public final BooleanProperty propertyNameHasError = new BooleanProperty();

    public final StringProperty propertyNameError = new StringProperty();

    public final RelayCommand commandClose = new RelayCommand(this::close);

    public final RelayCommand commandSave = new RelayCommand(this::save, this::canSave);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Actor actor;

    @Inject
    public ActorEditPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Actor actor) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_ACTOR, Parcels.wrap(actor));
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        actor = Parcels.unwrap(arguments.getParcelable(ARG_ACTOR));
        if (actor == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' must be not null.", ARG_ACTOR));
        }

        if (savedInstanceState != null) {
            actor = Parcels.unwrap(savedInstanceState.getParcelable(STATE_ACTOR));
        }

        propertyName.set(actor.getName());
        validateName();

        propertyName.addOnValueChangedListener(sender -> commandSave.raiseOnCanExecuteChanged());
        propertyName.addOnValueChangedListener(sender -> validateName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_ACTOR, Parcels.wrap(actor));
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        compositeDisposable.clear();
    }

    private void save() {
        actor.setName(propertyName.get());
        visionService.getUserActorApi().saveActor(actor);
        close();
        SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
    }

    private boolean canSave() {
        final String name = propertyName.get();
        return name != null && name.length() != 0;
    }

    private void close() {
        eventBus.post(CloseViewEvent.INSTANCE);
    }

    private void validateName() {
        String value = propertyName.get();
        if (value == null || value.length() == 0) {
            propertyNameError.set(resources.getString(R.string.input_error_required));
            propertyNameHasError.set(true);
        } else {
            propertyNameError.set(null);
            propertyNameHasError.set(false);
        }
    }

    public static final class CloseViewEvent {

        private static final CloseViewEvent INSTANCE = new CloseViewEvent();

        private CloseViewEvent() {
        }
    }

    public interface View {

    }
}
