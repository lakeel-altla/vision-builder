package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.binding.BinderFactory;
import com.lakeel.altla.android.binding.ParentViewContainer;
import com.lakeel.altla.android.binding.converter.ResourceToStringConverter;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.ResourceToColorFilterConverter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsPresenter;
import com.lakeel.altla.vision.builder.presentation.view.AreaSettingsView;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class AreaSettingsFragment extends AbstractFragment<AreaSettingsView, AreaSettingsPresenter>
        implements AreaSettingsView {

    @Inject
    AreaSettingsPresenter presenter;

    private InteractionListener interactionListener;

    @NonNull
    public static AreaSettingsFragment newInstance(@NonNull Scope scope) {
        AreaSettingsFragment fragment = new AreaSettingsFragment();
        Bundle bundle = AreaSettingsPresenter.createArguments(scope);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected AreaSettingsPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaSettingsView getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
        interactionListener = InteractionListener.class.cast(getParentFragment());
    }

    @Override
    protected void onDetachOverride() {
        super.onDetachOverride();

        interactionListener = null;
    }

    @Nullable
    @Override
    protected View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                    @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_area_settings, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        BinderFactory binderFactory = new BinderFactory(new ParentViewContainer(view));
        binderFactory.create(R.id.text_view_area_mode, "text", presenter.propertyAreaMode)
                     .converter(new ResourceToStringConverter(getResources()))
                     .bind();
        binderFactory.create(R.id.text_view_area_name, "text", presenter.propertyAreaName).bind();
        binderFactory.create(R.id.text_view_area_description_name, "text", presenter.propertyAreaDescriptionName)
                     .bind();
        binderFactory.create(R.id.image_button_area_description_list, "colorFilter",
                             presenter.propertyShowAreaDescriptionButtonColorFilter)
                     .converter(new ResourceToColorFilterConverter(getResources()))
                     .bind();
        binderFactory.create(R.id.image_button_close, "onClick", presenter.commandClose).bind();
        binderFactory.create(R.id.image_button_history, "onClick", presenter.commandShowHistory).bind();
        binderFactory.create(R.id.image_button_area_mode, "onClick", presenter.commandShowAreaMode).bind();
        binderFactory.create(R.id.image_button_area_find, "onClick", presenter.commandShowAreaFind).bind();
        binderFactory
                .create(R.id.image_button_area_description_list, "onClick", presenter.commandShowAreaDescriptionList)
                .bind();
        binderFactory.create(R.id.button_start, "onClick", presenter.commandStart).bind();
    }

    @Override
    public void onShowAreaSettingsHistoryView() {
        interactionListener.onShowAreaSettingsHistoryView();
    }

    @Override
    public void onShowAreaModeView(@NonNull Scope scope) {
        interactionListener.onShowAreaModeView(scope);
    }

    @Override
    public void onShowAreaFindView(@NonNull Scope scope) {
        interactionListener.onShowAreaFindView(scope);
    }

    @Override
    public void onShowAreaDescriptionByAreaListView(@NonNull Scope scope, @NonNull Area area) {
        interactionListener.onShowAreaDescriptionByAreaListView(scope, area);
    }

    @Override
    public void onUpdateArView(@NonNull String areaSettingsId) {
        interactionListener.onUpdateArView(areaSettingsId);
    }

    @Override
    public void onCloseView() {
        interactionListener.onCloseAreaSettingsView();
    }

    public void onAreaModeSelected(@NonNull Scope scope) {
        presenter.propertyAreaScope.set(scope);
    }

    public void onAreaSelected(@NonNull Area area) {
        presenter.propertyArea.set(area);
    }

    public void onAreaDescriptionSelected(@NonNull AreaDescription areaDescription) {
        presenter.propertyAreaDescription.set(areaDescription);
    }

    public void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                       @NonNull Area area,
                                       @NonNull AreaDescription areaDescription) {
        presenter.onAreaSettingsSelected(areaSettings, area, areaDescription);
    }

//    @ColorInt
//    private int resolveImageButtonTint(boolean enabled) {
//        int resId = enabled ? R.color.foreground_overlay : R.color.foreground_overlay_disabled;
//        return getResources().getColor(resId);
//    }

    public interface InteractionListener {

        void onShowAreaSettingsHistoryView();

        void onShowAreaModeView(@NonNull Scope scope);

        void onShowAreaFindView(@NonNull Scope scope);

        void onShowAreaDescriptionByAreaListView(@NonNull Scope scope, @NonNull Area area);

        void onUpdateArView(@NonNull String areaSettingsId);

        void onCloseAreaSettingsView();
    }
}
