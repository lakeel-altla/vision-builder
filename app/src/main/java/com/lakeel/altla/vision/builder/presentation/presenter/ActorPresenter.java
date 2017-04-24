package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.property.LongProperty;
import com.lakeel.altla.android.property.ObjectProperty;
import com.lakeel.altla.android.property.Property;
import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ActorPresenter extends BasePresenter<ActorPresenter.View> {

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    @Inject
    ArModel arModel;

    public final StringProperty propertyName = new StringProperty();

    public final LongProperty propertyCreatedAt = new LongProperty(-1);

    public final LongProperty propertyUpdatedAt = new LongProperty(-1);

    public final RelayCommand commandClose = new RelayCommand(this::close);

    public final RelayCommand commandShowEdit = new RelayCommand(this::showEdit);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final Property.OnValueChangedListener pickedActorOnValueChangedListener = sender -> {
        loadActor();
        commandShowEdit.raiseOnCanExecuteChanged();
    };

    private final ObjectProperty<Actor> actor = new ObjectProperty<>();

    @Inject
    public ActorPresenter() {
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        actor.addOnValueChangedListener(sender -> commandShowEdit.raiseOnCanExecuteChanged());
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        arModel.pickedActor.addOnValueChangedListener(pickedActorOnValueChangedListener);
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        arModel.pickedActor.removeOnValueChangedListener(pickedActorOnValueChangedListener);
    }

    @Override
    protected void onResumeOverride() {
        super.onResumeOverride();

        loadActor();
    }

    private void loadActor() {
        actor.set(null);
        propertyName.set(null);
        propertyCreatedAt.set(-1);
        propertyUpdatedAt.set(-1);

        ArModel.PickedActor pickedActor = arModel.pickedActor.get();

        if (pickedActor != null) {
            Disposable disposable = Maybe
                    .<Actor>create(e -> {
                        switch (pickedActor.scope) {
                            case PUBLIC:
                                visionService.getPublicActorApi().findActorById(pickedActor.actorId, actor -> {
                                    if (actor == null) {
                                        e.onComplete();
                                    } else {
                                        e.onSuccess(actor);
                                    }
                                }, e::onError);
                                break;
                            case USER:
                                visionService.getUserActorApi().findActorById(pickedActor.actorId, actor -> {
                                    if (actor == null) {
                                        e.onComplete();
                                    } else {
                                        e.onSuccess(actor);
                                    }
                                }, e::onError);
                                break;
                            default:
                                throw new IllegalStateException("Invalid scope: " + pickedActor.scope);
                        }
                    })
                    .subscribe(actor -> {
                        this.actor.set(actor);
                        propertyName.set(actor.getName());
                        propertyCreatedAt.set(actor.getCreatedAtAsLong());
                        propertyUpdatedAt.set(actor.getUpdatedAtAsLong());
                    }, e -> {
                        getLog().e("Failed.", e);
                        SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
                    }, () -> {
                        getLog().e("Entity not found.");
                        SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
                    });
            compositeDisposable.add(disposable);
        }
    }

    private void close() {
        eventBus.post(CloseViewEvent.INSTANCE);
    }

    private void showEdit() {
        final Actor value = actor.get();
        if (value != null) {
            eventBus.post(new ShowActorEditViewEvent(value));
        }
    }

    private boolean canShowEdit() {
        return actor.get() != null;
    }

    public static final class CloseViewEvent {

        private static final CloseViewEvent INSTANCE = new CloseViewEvent();

        private CloseViewEvent() {
        }
    }

    public static final class ShowActorEditViewEvent {

        @NonNull
        public final Actor actor;

        private ShowActorEditViewEvent(@NonNull Actor actor) {
            this.actor = actor;
        }
    }

    public interface View {

    }
}
