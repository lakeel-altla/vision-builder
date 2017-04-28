package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.tango.TangoIntents;
import com.lakeel.altla.vision.builder.presentation.event.ShowArViewEvent;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.app.Activity;
import android.content.Intent;

import javax.inject.Inject;

public final class TangoPermissionPresenter extends BasePresenter<TangoPermissionPresenter.View> {

    private static final int REQUEST_CODE = 888;

    @Inject
    EventBus eventBus;

    @Inject
    public TangoPermissionPresenter() {
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        confirmPermission();
    }

    public void confirmPermission() {
        getView().startActivityForResult(TangoIntents.createAdfLoadSaveRequestPermissionIntent(), REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE != requestCode) return;

        if (Activity.RESULT_CANCELED != resultCode) {
            eventBus.post(ShowArViewEvent.INSTANCE);
        } else {
            getView().showAreaLearningPermissionRequiredSnackbar();
        }
    }

    public interface View {

        void startActivityForResult(Intent intent, int requestCode);

        void showAreaLearningPermissionRequiredSnackbar();
    }
}
