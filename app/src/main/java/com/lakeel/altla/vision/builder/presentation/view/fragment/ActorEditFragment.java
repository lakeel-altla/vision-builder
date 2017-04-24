package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.ActorEditPresenter;
import com.lakeel.altla.vision.model.Actor;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public final class ActorEditFragment extends AbstractFragment<ActorEditPresenter.View, ActorEditPresenter>
        implements ActorEditPresenter.View {

    @Inject
    ActorEditPresenter presenter;

    @NonNull
    public static ActorEditFragment newInstance(@NonNull Actor actor) {
        ActorEditFragment fragment = new ActorEditFragment();
        Bundle bundle = ActorEditPresenter.createArguments(actor);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected ActorEditPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected ActorEditPresenter.View getViewInterface() {
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
        return inflater.inflate(R.layout.fragment_actor_edit, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ViewBindingFactory factory = new ViewBindingFactory(view);
        factory.create(R.id.text_input_edit_text_name, "text", presenter.propertyName).bind();
        factory.create(R.id.text_input_layout_name, "error", presenter.propertyNameError).bind();
        factory.create(R.id.text_input_layout_name, "errorEnabled", presenter.propertyNameHasError).bind();
        factory.create(R.id.image_button_close, "onClick", presenter.commandClose).bind();
        factory.create(R.id.button_save, "onClick", presenter.commandSave).bind();
    }
}
