package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.property.LongProperty;
import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.os.Bundle;
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

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public ActorPresenter() {
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        arModel.pickedActor.addOnValueChangedListener(sender -> {
            loadActor();
        });
    }

    @Override
    protected void onResumeOverride() {
        super.onResumeOverride();

        loadActor();
    }

    private void loadActor() {
        ArModel.PickedActor pickedActor = arModel.pickedActor.get();

        if (pickedActor == null) {
            propertyName.set(null);
            propertyCreatedAt.set(-1);
            propertyUpdatedAt.set(-1);
        } else {
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

    public static final class CloseViewEvent {

        private static final CloseViewEvent INSTANCE = new CloseViewEvent();

        private CloseViewEvent() {
        }
    }

    public interface View {

    }
}
