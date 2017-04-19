package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaDescriptionByAreaListPresenter;
import com.lakeel.altla.vision.builder.presentation.view.adapter.AreaDescriptionByAreaListAdapter;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class AreaDescriptionByAreaListFragment
        extends AbstractFragment<AreaDescriptionByAreaListPresenter.View, AreaDescriptionByAreaListPresenter>
        implements AreaDescriptionByAreaListPresenter.View {

    @Inject
    AreaDescriptionByAreaListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @NonNull
    public static AreaDescriptionByAreaListFragment newInstance(@NonNull Scope scope, @NonNull Area area) {
        AreaDescriptionByAreaListFragment fragment = new AreaDescriptionByAreaListFragment();
        Bundle bundle = AreaDescriptionByAreaListPresenter.createArguments(scope, area);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected AreaDescriptionByAreaListPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaDescriptionByAreaListPresenter.View getViewInterface() {
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
        return inflater.inflate(R.layout.fragment_area_description_by_area_list, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);

        recyclerView.setAdapter(new AreaDescriptionByAreaListAdapter(presenter));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ViewBindingFactory factory = new ViewBindingFactory(view);
        factory.create(R.id.image_button_close, "onClick", presenter.commandClose).bind();
        factory.create(R.id.button_select, "onClick", presenter.commandSelect).bind();
    }

    @Override
    public void onDataSetChanged() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSnackbar(@StringRes int resId) {
        if (getView() != null) {
            Snackbar.make(getView(), resId, Snackbar.LENGTH_SHORT).show();
        }
    }
}
