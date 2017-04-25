package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.property.IntProperty;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public final class AreaModePresenter extends BasePresenter<AreaModePresenter.View> {

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    public final IntProperty propertyChckedButton = new IntProperty(R.id.radio_button_public);

    @Inject
    public AreaModePresenter() {
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        final Scope scope = selectAreaSettingsModel.getAreaScope();
        propertyChckedButton.set(resolveCheckedId(scope));
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(ActionBarVisibleEvent.VISIBLE);
        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_area_mode_view)));
        eventBus.post(HomeAsUpVisibleEvent.VISIBLE);
        eventBus.post(new HomeAsUpIndicatorEvent(resources.getDrawable(R.drawable.ic_arrow_back_white_24dp)));
    }

    public void select() {
        final Scope scope = resolveScope(propertyChckedButton.get());
        selectAreaSettingsModel.selectAreaScope(scope);
        eventBus.post(new BackViewEvent(getView()));
    }

    @IdRes
    private static int resolveCheckedId(@NonNull Scope scope) {
        return (scope == Scope.PUBLIC) ? R.id.radio_button_public : R.id.radio_button_user;
    }

    @NonNull
    private static Scope resolveScope(@IdRes int checkedId) {
        return (checkedId == R.id.radio_button_public) ? Scope.PUBLIC : Scope.USER;
    }

    public interface View {

    }
}
