package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.LongToDateFormatConverter;
import com.lakeel.altla.vision.builder.presentation.presenter.ActorPresenter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class ActorFragment extends AbstractFragment<ActorPresenter.View, ActorPresenter>
        implements ActorPresenter.View {

    @Inject
    ActorPresenter presenter;

    @NonNull
    public static ActorFragment newInstance() {
        return new ActorFragment();
    }

    @Override
    protected ActorPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected ActorPresenter.View getViewInterface() {
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
        return inflater.inflate(R.layout.fragment_actor, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ViewBindingFactory factory = new ViewBindingFactory(view);
        factory.create(R.id.text_view_name, "text", presenter.propertyName).bind();
        factory.create(R.id.text_view_created_at, "text", presenter.propertyCreatedAt)
               .converter(new LongToDateFormatConverter(getContext()))
               .bind();
        factory.create(R.id.text_view_updated_at, "text", presenter.propertyUpdatedAt)
               .converter(new LongToDateFormatConverter(getContext()))
               .bind();
        factory.create(R.id.image_button_close, "onClick", presenter.commandClose).bind();
    }

    @Override
    public void onSnackbar(@StringRes int resId) {
        if (getView() != null) {
            Snackbar.make(getView(), resId, Snackbar.LENGTH_SHORT).show();
        }
    }
}
