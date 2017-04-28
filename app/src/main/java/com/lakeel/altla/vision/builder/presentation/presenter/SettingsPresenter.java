package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.firebase.auth.FirebaseAuth;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ClearBackStackEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowSignInViewEvent;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;

import javax.inject.Inject;

public final class SettingsPresenter extends BasePresenter<SettingsPresenter.View> {

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    public final RelayCommand commandSignOut = new RelayCommand(this::signOut);

    @Inject
    public SettingsPresenter() {
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(ActionBarVisibleEvent.VISIBLE);
        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_settings_view)));
        eventBus.post(HomeAsUpVisibleEvent.VISIBLE);
        eventBus.post(new HomeAsUpIndicatorEvent(resources.getDrawable(R.drawable.ic_clear_white_24dp)));
    }

    private void signOut() {
        visionService.getUserDeviceConnectionApi()
                     .markUserDeviceConnectionAsOffline(aVoid -> {
                         FirebaseAuth.getInstance().signOut();
                         eventBus.post(ClearBackStackEvent.INSTANCE);
                         eventBus.post(ShowSignInViewEvent.INSTANCE);
                     }, e -> {
                         getLog().e("Failed.", e);
                         FirebaseAuth.getInstance().signOut();
                         eventBus.post(ClearBackStackEvent.INSTANCE);
                         eventBus.post(ShowSignInViewEvent.INSTANCE);
                     });
    }

    public interface View {

    }
}
