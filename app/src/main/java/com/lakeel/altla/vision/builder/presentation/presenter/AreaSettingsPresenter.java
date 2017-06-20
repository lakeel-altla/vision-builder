package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.property.IntProperty;
import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaDescriptionByAreaListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaFindViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaModeViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.ShowAreaSettingsListViewEvent;
import com.lakeel.altla.vision.builder.presentation.helper.StringResourceHelper;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public final class AreaSettingsPresenter extends BasePresenter<AreaSettingsPresenter.View> {

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    public final IntProperty propertyAreaMode = new IntProperty(
            StringResourceHelper.resolveScopeStringResource(Scope.USER));

    public final StringProperty propertyAreaName = new StringProperty();

    public final StringProperty propertyAreaDescriptionName = new StringProperty();

    public final IntProperty propertyShowAreaDescriptionButtonColorFilter = new IntProperty(
            R.color.foreground_overlay_disabled);

    public final RelayCommand commandShowAreaMode = new RelayCommand(this::showAreaMode);

    public final RelayCommand commandShowAreaFind = new RelayCommand(this::showAreaFind);

    public final RelayCommand commandShowAreaDescriptionList = new RelayCommand(this::showAreaDescriptionList,
                                                                                this::canShowAreaDescriptionList);

    public final RelayCommand commandStart = new RelayCommand(this::start, this::canStart);

    @Inject
    public AreaSettingsPresenter() {
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        commandShowAreaDescriptionList.addOnCanExecuteChangedListener(this::updateShowAreaDescriptionButtonColorFilter);
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_area_settings_view)));
        eventBus.post(new HomeAsUpIndicatorEvent(resources.getDrawable(R.drawable.ic_clear_white_24dp)));
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        final Scope areaScope = selectAreaSettingsModel.getAreaScope();
        final Area area = selectAreaSettingsModel.getArea();
        final AreaDescription areaDescription = selectAreaSettingsModel.getAreaDescription();

        propertyAreaMode.setValue(StringResourceHelper.resolveScopeStringResource(areaScope));
        propertyAreaName.set(area == null ? null : area.getName());
        propertyAreaDescriptionName.set(areaDescription == null ? null : areaDescription.getName());

        updateShowAreaDescriptionButtonColorFilter();
    }

    public void showAreaSettingsListView() {
        eventBus.post(ShowAreaSettingsListViewEvent.INSTANCE);
    }

    private void updateShowAreaDescriptionButtonColorFilter() {
        boolean enabled = canShowAreaDescriptionList();
        int id = enabled ? R.color.background_image_button : R.color.background_image_button_disabled;
        propertyShowAreaDescriptionButtonColorFilter.set(id);
    }

    private void showAreaMode() {
        eventBus.post(ShowAreaModeViewEvent.INSTANCE);
    }

    private void showAreaFind() {
        eventBus.post(ShowAreaFindViewEvent.INSTANCE);
    }

    private void showAreaDescriptionList() {
        eventBus.post(ShowAreaDescriptionByAreaListViewEvent.INSTANCE);
    }

    private boolean canShowAreaDescriptionList() {
        return selectAreaSettingsModel.getArea() != null;
    }

    private void start() {
        if (!canStart()) return;

        selectAreaSettingsModel.start();
        eventBus.post(new BackViewEvent(getView()));
    }

    private boolean canStart() {
        return selectAreaSettingsModel.canStart();
    }

    public interface View {

    }
}
