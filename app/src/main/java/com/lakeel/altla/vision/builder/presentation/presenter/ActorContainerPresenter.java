package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.vision.builder.presentation.view.ActorContainerView;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public final class ActorContainerPresenter extends BasePresenter<ActorContainerView> {

    private boolean showInitialView;

    @Inject
    public ActorContainerPresenter() {
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        showInitialView = (savedInstanceState == null);
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        if (showInitialView) {
            getView().onShowActorView();
        }
    }
}
