package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.IntProperty;
import com.lakeel.altla.android.binding.property.ObjectProperty;
import com.lakeel.altla.android.binding.property.StringProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.CurrentUser;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.StringResourceHelper;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.parceler.Parcels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public final class AreaSettingsPresenter extends BasePresenter<AreaSettingsPresenter.View> {

    private static final String ARG_AREA_SCOPE = "areaScope";

    private static final String STATE_AREA_SETTINGS = "areaSettings";

    private static final String STATE_AREA_SCOPE = "areaScope";

    private static final String STATE_AREA = "area";

    private static final String STATE_AREA_DESCRIPTION = "areaDescription";

    @Inject
    VisionService visionService;

    private ParentView parentView;

    private AreaSettings areaSettings;

    public final ObjectProperty<Scope> propertyAreaScope = new ObjectProperty<>(Scope.PUBLIC);

    public final ObjectProperty<Area> propertyArea = new ObjectProperty<>();

    public final ObjectProperty<AreaDescription> propertyAreaDescription = new ObjectProperty<>();

    public final IntProperty propertyAreaMode = new IntProperty(
            StringResourceHelper.resolveScopeStringResource(Scope.PUBLIC));

    public final StringProperty propertyAreaName = new StringProperty();

    public final StringProperty propertyAreaDescriptionName = new StringProperty();

    public final IntProperty propertyShowAreaDescriptionButtonColorFilter = new IntProperty(
            R.color.foreground_overlay_disabled);

    public final RelayCommand commandClose = new RelayCommand(this::close);

    public final RelayCommand commandShowHistory = new RelayCommand(this::showHistory);

    public final RelayCommand commandShowAreaMode = new RelayCommand(this::showAreaMode);

    public final RelayCommand commandShowAreaFind = new RelayCommand(this::showAreaFind);

    public final RelayCommand commandShowAreaDescriptionList = new RelayCommand(this::showAreaDescriptionList,
                                                                                this::canShowAreaDescriptionList);

    public final RelayCommand commandStart = new RelayCommand(this::start, this::canStart);

    @Inject
    public AreaSettingsPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Scope scope) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_AREA_SCOPE, Parcels.wrap(scope));
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        Scope initialScope = Parcels.unwrap(arguments.getParcelable(ARG_AREA_SCOPE));
        if (initialScope == null) {
            throw new ArgumentNullException(String.format("Argument '%s' is required.", ARG_AREA_SCOPE));
        }

        propertyAreaScope.addOnValueChangedListener(sender -> {
            propertyAreaMode.set(StringResourceHelper.resolveScopeStringResource(propertyAreaScope.get()));
            propertyArea.set(null);
        });

        propertyArea.addOnValueChangedListener(sender -> {
            Area area = propertyArea.get();
            propertyAreaName.set(area == null ? null : area.getName());
            propertyAreaDescription.set(null);
            commandShowAreaDescriptionList.raiseOnCanExecuteChanged();
        });

        propertyAreaDescription.addOnValueChangedListener(sender -> {
            AreaDescription areaDescription = propertyAreaDescription.get();
            propertyAreaDescriptionName.set(areaDescription == null ? null : areaDescription.getName());
            commandStart.raiseOnCanExecuteChanged();
        });

        propertyAreaScope.set(initialScope);

        if (savedInstanceState != null) {
            areaSettings = Parcels.unwrap(savedInstanceState.getParcelable(STATE_AREA_SETTINGS));

            Scope areaScope = Parcels.unwrap(savedInstanceState.getParcelable(STATE_AREA_SCOPE));
            if (areaScope != null) propertyAreaScope.set(areaScope);

            Area area = Parcels.unwrap(savedInstanceState.getParcelable(STATE_AREA));
            if (area != null) propertyArea.set(area);

            AreaDescription areaDescription = Parcels.unwrap(savedInstanceState.getParcelable(STATE_AREA_DESCRIPTION));
            if (areaDescription != null) propertyAreaDescription.set(areaDescription);
        }

        commandShowAreaDescriptionList.addOnCanExecuteChangedListener(this::updateShowAreaDescriptionButtonColorFilter);

        updateShowAreaDescriptionButtonColorFilter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_AREA_SETTINGS, Parcels.wrap(areaSettings));
        outState.putParcelable(STATE_AREA_SCOPE, Parcels.wrap(propertyAreaScope.get()));
        outState.putParcelable(STATE_AREA, Parcels.wrap(propertyArea.get()));
        outState.putParcelable(STATE_AREA_DESCRIPTION, Parcels.wrap(propertyAreaDescription.get()));
    }

    public void onParentViewAttached(@NonNull ParentView parentView) {
        this.parentView = parentView;
    }

    public void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                       @NonNull Area area,
                                       @NonNull AreaDescription areaDescription) {
        this.areaSettings = areaSettings;

        propertyAreaScope.set(areaSettings.getAreaScopeAsEnum());
        propertyArea.set(area);
        propertyAreaDescription.set(areaDescription);
    }

    private void updateShowAreaDescriptionButtonColorFilter() {
        boolean enabled = commandShowAreaDescriptionList.canExecute();
        int id = enabled ? R.color.foreground_overlay : R.color.foreground_overlay_disabled;
        propertyShowAreaDescriptionButtonColorFilter.set(id);
    }

    private void close() {
        parentView.onCloseAreaSettingsView();
    }

    private void showHistory() {
        parentView.onShowAreaSettingsHistoryView();
    }

    private void showAreaMode() {
        Scope scope = propertyAreaScope.get();
        if (scope != null) {
            parentView.onShowAreaModeView(scope);
        }
    }

    private void showAreaFind() {
        Scope scope = propertyAreaScope.get();
        if (scope != null) {
            parentView.onShowAreaFindView(scope);
        }
    }

    private void showAreaDescriptionList() {
        Scope scope = propertyAreaScope.get();
        Area area = propertyArea.get();
        if (scope != null && area != null) {
            parentView.onShowAreaDescriptionByAreaListView(scope, area);
        }
    }

    private boolean canShowAreaDescriptionList() {
        return propertyArea.get() != null;
    }

    private void start() {
        if (!canStart()) return;

        if (areaSettings == null) {
            areaSettings = new AreaSettings();
            areaSettings.setUserId(CurrentUser.getInstance().getUserId());
        }

        areaSettings.setAreaScopeAsEnum(propertyAreaScope.get());
        areaSettings.setAreaId(propertyArea.get().getId());
        areaSettings.setAreaDescriptionId(propertyAreaDescription.get().getId());

        visionService.getUserAreaSettingsApi()
                     .saveUserAreaSettings(areaSettings);

        parentView.onUpdateArView(areaSettings.getId());
        parentView.onCloseAreaSettingsView();
    }

    private boolean canStart() {
        return propertyAreaScope.get() != null &&
               propertyArea.get() != null &&
               propertyAreaDescription.get() != null;
    }

    public interface View {

    }

    public interface ParentView {

        void onShowAreaSettingsHistoryView();

        void onShowAreaModeView(@NonNull Scope scope);

        void onShowAreaFindView(@NonNull Scope scope);

        void onShowAreaDescriptionByAreaListView(@NonNull Scope scope, @NonNull Area area);

        void onUpdateArView(@NonNull String areaSettingsId);

        void onCloseAreaSettingsView();
    }
}
