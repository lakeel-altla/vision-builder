package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.ObjectProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.view.AreaSettingsView;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.parceler.Parcels;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public final class AreaSettingsPresenter extends BasePresenter<AreaSettingsView> {

    private static final String ARG_SCOPE = "scope";

    @Inject
    VisionService visionService;

    @Inject
    Resources resources;

    private AreaSettings areaSettings;

    @NonNull
    public ObjectProperty<Scope> propertyScope = new ObjectProperty<Scope>(Scope.PUBLIC) {
        @Override
        protected void onValueChanged(@Nullable Scope oldValue, @Nullable Scope newValue) {
            super.onValueChanged(oldValue, newValue);

            if (newValue == null) {
                propertyAreaMode.set(null);
            } else {
                int resId =
                        (newValue == Scope.PUBLIC ? R.string.label_area_mode_public : R.string.label_area_mode_user);
                propertyAreaMode.set(resources.getString(resId));
            }

            propertyArea.set(null);
        }
    };

    @NonNull
    public ObjectProperty<Area> propertyArea = new ObjectProperty<Area>() {
        @Override
        protected void onValueChanged(@Nullable Area oldValue, @Nullable Area newValue) {
            super.onValueChanged(oldValue, newValue);

            propertyAreaName.set(newValue == null ? null : newValue.getName());
            propertyAreaDescription.set(null);

            commandShowAreaDescriptionList.raiseOnCanExecuteChanged();
        }
    };

    @NonNull
    public ObjectProperty<AreaDescription> propertyAreaDescription = new ObjectProperty<AreaDescription>() {
        @Override
        protected void onValueChanged(@Nullable AreaDescription oldValue, @Nullable AreaDescription newValue) {
            super.onValueChanged(oldValue, newValue);

            propertyAreaDescriptionName.set(newValue == null ? null : newValue.getName());

            commandStart.raiseOnCanExecuteChanged();
        }
    };

    @NonNull
    public ObjectProperty<String> propertyAreaMode = new ObjectProperty<>();

    @NonNull
    public ObjectProperty<String> propertyAreaName = new ObjectProperty<>();

    @NonNull
    public ObjectProperty<String> propertyAreaDescriptionName = new ObjectProperty<>();

    @NonNull
    public RelayCommand commandClose = new RelayCommand(() -> getView().onCloseView());

    @NonNull
    public RelayCommand commandShowHistory = new RelayCommand(() -> getView().onShowAreaSettingsHistoryView());

    @NonNull
    public RelayCommand commandShowAreaMode = new RelayCommand(() -> {
        Scope value = propertyScope.get();
        if (value != null) {
            getView().onShowAreaModeView(value);
        }
    });

    @NonNull
    public RelayCommand commandShowAreaFind = new RelayCommand(() -> {
        Scope value = propertyScope.get();
        if (value != null) {
            getView().onShowAreaFindView(value);
        }
    });

    @NonNull
    public RelayCommand commandShowAreaDescriptionList = new RelayCommand
            (() -> {
                Scope scopeValue = propertyScope.get();
                Area areaValue = propertyArea.get();
                if (scopeValue != null && areaValue != null) {
                    getView().onShowAreaDescriptionByAreaListView(scopeValue, areaValue);
                }
            },
             () -> propertyArea.get() != null
            );

    @NonNull
    public RelayCommand commandStart = new RelayCommand(this::start, this::canStart);

    @Inject
    public AreaSettingsPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Scope scope) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SCOPE, Parcels.wrap(scope));
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        Scope initialScope = Parcels.unwrap(arguments.getParcelable(ARG_SCOPE));
        if (initialScope == null) {
            throw new ArgumentNullException(String.format("Argument '%s' is required.", ARG_SCOPE));
        }

        propertyScope.set(initialScope);

        // TODO: restore saved fields.
    }

    public void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                       @NonNull Area area,
                                       @NonNull AreaDescription areaDescription) {
        this.areaSettings = areaSettings;

        propertyScope.set(areaSettings.getAreaScopeAsEnum());
        propertyArea.set(area);
        propertyAreaDescription.set(areaDescription);
    }

    private void start() {
        if (!canStart()) return;

        if (areaSettings == null) {
            areaSettings = new AreaSettings();
            areaSettings.setUserId(CurrentUser.getInstance().getUserId());
        }

        areaSettings.setAreaScopeAsEnum(propertyScope.get());
        areaSettings.setAreaId(propertyArea.get().getId());
        areaSettings.setAreaDescriptionId(propertyAreaDescription.get().getId());

        visionService.getUserAreaSettingsApi()
                     .saveUserAreaSettings(areaSettings);

        getView().onUpdateArView(areaSettings.getId());
        getView().onCloseView();
    }

    private boolean canStart() {
        return propertyScope.get() != null && propertyArea.get() != null && propertyAreaDescription.get() != null;
    }
}
