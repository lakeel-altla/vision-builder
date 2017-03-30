package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.view.ActorView;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.parceler.Parcels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ActorPresenter extends BasePresenter<ActorView> {

    private static final String ARG_SCOPE = "scope";

    private static final String ARG_ACTOR_ID = "actorId";

    private static final String STATE_SCOPE = "scopeValue";

    private static final String STATE_ACTOR_ID = "actorId";

    @Inject
    VisionService visionService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Scope scope;

    private String actorId;

    @Inject
    public ActorPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Scope scope, @NonNull String actorId) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SCOPE, Parcels.wrap(scope));
        bundle.putString(ARG_ACTOR_ID, actorId);
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        if (savedInstanceState == null) {
            scope = Parcels.unwrap(arguments.getParcelable(ARG_SCOPE));
            if (scope == null) {
                throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_SCOPE));
            }

            actorId = arguments.getString(ARG_ACTOR_ID);
            if (actorId == null) {
                throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_ACTOR_ID));
            }
        } else {
            scope = Parcels.unwrap(savedInstanceState.getParcelable(STATE_SCOPE));
            if (scope == null) {
                throw new IllegalArgumentException(String.format("State '%s' is required.", STATE_SCOPE));
            }

            actorId = savedInstanceState.getString(STATE_ACTOR_ID);
            if (actorId == null) {
                throw new IllegalArgumentException(String.format("State '%s' is required.", STATE_ACTOR_ID));
            }
        }
    }

    @Override
    protected void onResumeOverride() {
        super.onResumeOverride();

        loadActor();
    }

    public void onClickImageButtonClose() {
        getView().onCloseView();
        getView().onUpdateMainMenuVisible(true);
    }

    public void onUpdateActor(@NonNull Scope scope, @Nullable String actorId) {
        this.scope = scope;
        this.actorId = actorId;

        loadActor();
    }

    private void loadActor() {
        if (actorId == null) {
            getView().onUpdateName(null);
            getView().onUpdateCreatedAt(-1);
            getView().onUpdateUpdatedAt(-1);
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
                        getView().onUpdateName(actor.getName());
                        getView().onUpdateCreatedAt(actor.getCreatedAtAsLong());
                        getView().onUpdateUpdatedAt(actor.getUpdatedAtAsLong());
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
}
