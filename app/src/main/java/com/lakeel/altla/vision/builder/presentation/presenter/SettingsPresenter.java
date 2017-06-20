package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.firebase.auth.FirebaseAuth;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.presentation.event.ShowSignInViewEvent;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

public final class SettingsPresenter extends BasePresenter<SettingsPresenter.View> {

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    public final RelayCommand commandSignOut = new RelayCommand(this::signOut);

    @Inject
    public SettingsPresenter() {
    }

    private void signOut() {
        visionService.getUserDeviceConnectionApi()
                     .markUserDeviceConnectionAsOffline(aVoid -> {
                         showSignInView();
                     }, e -> {
                         getLog().e("Failed.", e);
                         showSignInView();
                     });
    }

    private void showSignInView() {
        FirebaseAuth.getInstance().signOut();
        eventBus.post(ShowSignInViewEvent.INSTANCE);
    }

    public interface View {

    }
}
