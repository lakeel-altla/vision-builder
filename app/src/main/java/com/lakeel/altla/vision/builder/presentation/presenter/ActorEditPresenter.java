package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ActorEditPresenter extends BasePresenter<ActorEditPresenter.View> {

    private static final String ARG_AREA_ID = "areaId";

    private static final String ARG_ACTOR_ID = "actorId";

    private static final String STATE_ACTOR = "actor";

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String areaId;

    private String actorId;

    private Actor actor;

    @Inject
    public ActorEditPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull String sceneId, @NonNull String actorId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_AREA_ID, sceneId);
        bundle.putString(ARG_ACTOR_ID, actorId);
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        String areaId = arguments.getString(ARG_AREA_ID);
        if (areaId == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' must be not null.", ARG_AREA_ID));
        }

        String actorId = arguments.getString(ARG_ACTOR_ID);
        if (actorId == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' must be not null.", ARG_ACTOR_ID));
        }

        this.areaId = areaId;
        this.actorId = actorId;

        if (savedInstanceState == null) {
            actor = null;
        } else {
            actor = Parcels.unwrap(savedInstanceState.getParcelable(STATE_ACTOR));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_ACTOR, Parcels.wrap(actor));
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        getView().onUpdateTitle(actorId);
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        getView().onUpdateHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        getView().onUpdateViewsEnabled(false);
        getView().onUpdateActionSave(false);

        if (actor == null) {
            Disposable disposable = Maybe.<Actor>create(e -> {
                visionService.getUserActorApi().findActorById(actorId, actor -> {
                    if (actor == null) {
                        e.onComplete();
                    } else {
                        e.onSuccess(actor);
                    }
                }, e::onError);
            }).subscribe(actor -> {
                this.actor = actor;
                updateViews(actor);
                getView().onUpdateViewsEnabled(true);
                getView().onUpdateActionSave(canSave());
            }, e -> {
                getLog().e("Failed.", e);
                SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
            }, () -> {
                getLog().e("Entity not found.");
                SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
            });
            compositeDisposable.add(disposable);
        } else {
            updateViews(actor);
            getView().onUpdateViewsEnabled(true);
            getView().onUpdateActionSave(canSave());
        }
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        compositeDisposable.clear();
        getView().onUpdateHomeAsUpIndicator(null);
    }

    public void onEditTextNameAfterTextChanged(String value) {
        actor.setName(value);
        getView().onHideNameError();

        // Don't save the empty name.
        if (actor.getName() == null || actor.getName().length() == 0) {
            getView().onShowNameError(R.string.input_error_required);
        }

        getView().onUpdateActionSave(canSave());
    }

    public void onEditTextPositionXAfterTextChanged(String value) {
        actor.setPositionX(Double.parseDouble(value));
    }

    public void onEditTextPositionYAfterTextChanged(String value) {
        actor.setPositionY(Double.parseDouble(value));
    }

    public void onEditTextPositionZAfterTextChanged(String value) {
        actor.setPositionZ(Double.parseDouble(value));
    }

    public void onEditTextOrientationXAfterTextChanged(String value) {
        actor.setOrientationX(Double.parseDouble(value));
    }

    public void onEditTextOrientationYAfterTextChanged(String value) {
        actor.setOrientationY(Double.parseDouble(value));
    }

    public void onEditTextOrientationZAfterTextChanged(String value) {
        actor.setOrientationZ(Double.parseDouble(value));
    }

    public void onEditTextOrientationWAfterTextChanged(String value) {
        actor.setOrientationW(Double.parseDouble(value));
    }

    public void onEditTextScaleXAfterTextChanged(String value) {
        actor.setScaleX(Double.parseDouble(value));

        getView().onUpdateActionSave(canSave());
    }

    public void onEditTextScaleYAfterTextChanged(String value) {
        actor.setScaleY(Double.parseDouble(value));

        getView().onUpdateActionSave(canSave());
    }

    public void onEditTextScaleZAfterTextChanged(String value) {
        actor.setScaleZ(Double.parseDouble(value));

        getView().onUpdateActionSave(canSave());
    }

    public void onActionSave() {
        getView().onUpdateViewsEnabled(false);
        getView().onUpdateActionSave(false);

        visionService.getUserActorApi().saveActor(actor);
        getView().onBackView();

        SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
    }

    private boolean canSave() {
        return 0 < actor.getScaleX() &&
               0 < actor.getScaleY() &&
               0 < actor.getScaleZ() &&
               actor.getName() != null &&
               actor.getName().length() != 0;
    }

    private void updateViews(@NonNull Actor actor) {
        getView().onUpdateName(actor.getName());
        getView().onUpdatePositionX(actor.getPositionX());
        getView().onUpdatePositionY(actor.getPositionY());
        getView().onUpdatePositionZ(actor.getOrientationZ());
        getView().onUpdateOrientationX(actor.getOrientationX());
        getView().onUpdateOrientationY(actor.getOrientationY());
        getView().onUpdateOrientationZ(actor.getOrientationZ());
        getView().onUpdateOrientationW(actor.getOrientationW());
        getView().onUpdateScaleX(actor.getScaleX());
        getView().onUpdateScaleY(actor.getScaleY());
        getView().onUpdateScaleZ(actor.getScaleZ());
    }

    public interface View {

        void onUpdateViewsEnabled(boolean enabled);

        void onUpdateActionSave(boolean enabled);

        void onUpdateHomeAsUpIndicator(@DrawableRes int resId);

        void onUpdateHomeAsUpIndicator(@Nullable Drawable drawable);

        void onUpdateTitle(@Nullable String title);

        void onUpdateName(@Nullable String name);

        void onUpdatePositionX(double x);

        void onUpdatePositionY(double y);

        void onUpdatePositionZ(double z);

        void onUpdateOrientationX(double x);

        void onUpdateOrientationY(double y);

        void onUpdateOrientationZ(double z);

        void onUpdateOrientationW(double w);

        void onUpdateScaleX(double x);

        void onUpdateScaleY(double y);

        void onUpdateScaleZ(double z);

        void onShowNameError(@StringRes int resId);

        void onHideNameError();

        void onBackView();
    }
}
