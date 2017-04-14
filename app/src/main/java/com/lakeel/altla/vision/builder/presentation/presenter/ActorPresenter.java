package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.LongProperty;
import com.lakeel.altla.android.binding.property.StringProperty;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.CloseViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowActorEvent;
import com.lakeel.altla.vision.builder.presentation.view.ActorContainerView;
import com.lakeel.altla.vision.builder.presentation.view.ActorView;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ActorPresenter extends BasePresenter<ActorView> {

    private static final String STATE_SCOPE = "scopeValue";

    private static final String STATE_ACTOR_ID = "actorId";

    @Inject
    VisionService visionService;

    public final StringProperty propertyName = new StringProperty();

    public final LongProperty propertyCreatedAt = new LongProperty(-1);

    public final LongProperty propertyUpdatedAt = new LongProperty(-1);

    public final RelayCommand commandClose = new RelayCommand(this::close);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Scope scope;

    private String actorId;

    @Inject
    public ActorPresenter() {
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (savedInstanceState != null) {
            scope = Parcels.unwrap(savedInstanceState.getParcelable(STATE_SCOPE));
            actorId = savedInstanceState.getString(STATE_ACTOR_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_SCOPE, Parcels.wrap(scope));
        outState.putString(STATE_ACTOR_ID, actorId);
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

    @Subscribe(sticky = true)
    public void onEvent(@NonNull ShowActorEvent event) {
        this.scope = event.scope;
        this.actorId = event.actorId;

        loadActor();
    }

    private void loadActor() {
        if (scope == null || actorId == null) {
            propertyName.set(null);
            propertyCreatedAt.set(-1);
            propertyUpdatedAt.set(-1);
        } else {
            Disposable disposable = Maybe
                    .<Actor>create(e -> {
                        switch (scope) {
                            case PUBLIC:
                                visionService.getPublicActorApi().findActorById(actorId, actor -> {
                                    if (actor == null) {
                                        e.onComplete();
                                    } else {
                                        e.onSuccess(actor);
                                    }
                                }, e::onError);
                                break;
                            case USER:
                                visionService.getUserActorApi().findActorById(actorId, actor -> {
                                    if (actor == null) {
                                        e.onComplete();
                                    } else {
                                        e.onSuccess(actor);
                                    }
                                }, e::onError);
                                break;
                            default:
                                throw new IllegalStateException("Invalid scope: " + scope);
                        }
                    })
                    .subscribe(actor -> {
                        propertyName.set(actor.getName());
                        propertyCreatedAt.set(actor.getCreatedAtAsLong());
                        propertyUpdatedAt.set(actor.getUpdatedAtAsLong());
                    }, e -> {
                        getLog().e("Failed.", e);
                        getView().onSnackbar(R.string.snackbar_failed);
                    }, () -> {
                        getLog().e("Entity not found.");
                        getView().onSnackbar(R.string.snackbar_failed);
                    });
            compositeDisposable.add(disposable);
        }
    }

    private void close() {
        EventBus.getDefault().post(new CloseViewEvent(ActorContainerView.class));
    }
}
