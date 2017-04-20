package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaModePresenter;
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

public final class AreaModeFragment extends AbstractFragment<AreaModePresenter.View, AreaModePresenter>
        implements AreaModePresenter.View {

    @Inject
    AreaModePresenter presenter;

    @NonNull
    public static AreaModeFragment newInstance(@NonNull Scope scope) {
        AreaModeFragment fragment = new AreaModeFragment();
        Bundle bundle = AreaModePresenter.createArguments(scope);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected AreaModePresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaModePresenter.View getViewInterface() {
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
        return inflater.inflate(R.layout.fragment_area_mode, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ViewBindingFactory factory = new ViewBindingFactory(view);
        factory.create(R.id.radio_group_scope, "checkedButton", presenter.propertyChckedButton).bind();
        factory.create(R.id.button_select, "onClick", presenter.commandSelect).bind();
        factory.create(R.id.image_button_close, "onClick", presenter.commandClose).bind();
    }
}
