package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.android.binding.converter.ResourceToStringConverter;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.ResourceToColorFilterConverter;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsPresenter;
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

public final class AreaSettingsFragment extends AbstractFragment<AreaSettingsPresenter.View, AreaSettingsPresenter>
        implements AreaSettingsPresenter.View {

    @Inject
    AreaSettingsPresenter presenter;

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
    protected AreaSettingsPresenter.View getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
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

        ViewBindingFactory factory = new ViewBindingFactory(view);
        factory.create(R.id.text_view_area_mode, "text", presenter.propertyAreaMode)
               .converter(new ResourceToStringConverter(getResources()))
               .bind();
        factory.create(R.id.text_view_area_name, "text", presenter.propertyAreaName).bind();
        factory.create(R.id.text_view_area_description_name, "text", presenter.propertyAreaDescriptionName)
               .bind();
        factory.create(R.id.image_button_area_description_list, "colorFilter",
                       presenter.propertyShowAreaDescriptionButtonColorFilter)
               .converter(new ResourceToColorFilterConverter(getResources()))
               .bind();
        factory.create(R.id.image_button_close, "onClick", presenter.commandClose).bind();
        factory.create(R.id.image_button_history, "onClick", presenter.commandShowHistory).bind();
        factory.create(R.id.image_button_area_mode, "onClick", presenter.commandShowAreaMode).bind();
        factory.create(R.id.image_button_area_find, "onClick", presenter.commandShowAreaFind).bind();
        factory.create(R.id.image_button_area_description_list, "onClick", presenter.commandShowAreaDescriptionList)
               .bind();
        factory.create(R.id.button_start, "onClick", presenter.commandStart).bind();
    }
}
