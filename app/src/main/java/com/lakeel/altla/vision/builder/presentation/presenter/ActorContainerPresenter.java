package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.support.annotation.NonNull;

import javax.inject.Inject;

public final class ActorContainerPresenter extends BasePresenter<ActorContainerPresenter.View> {

    @Inject
    EventBus eventBus;

    @Inject
    public ActorContainerPresenter() {
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        eventBus.register(this);
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(@NonNull ActorPresenter.ShowActorEditViewEvent event) {
        getView().showActorEditView(event.actor);
    }

    @Subscribe
    public void onEvent(@NonNull ActorEditPresenter.CloseViewEvent event) {
        getView().backView();
    }

    public interface View {

        void showActorEditView(@NonNull Actor actor);

        void backView();
    }
}
