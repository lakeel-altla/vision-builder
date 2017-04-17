package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.android.binding.ViewBindingFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaByPlaceListPresenter;
import com.lakeel.altla.vision.builder.presentation.view.adapter.AreaByPlaceListAdapter;
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
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class AreaByPlaceListFragment
        extends AbstractFragment<AreaByPlaceListPresenter.View, AreaByPlaceListPresenter>
        implements AreaByPlaceListPresenter.View {

    @Inject
    AreaByPlaceListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private InteractionListener interactionListener;

    @NonNull
    public static AreaByPlaceListFragment newInstance(@NonNull Scope scope, @NonNull Place place) {
        AreaByPlaceListFragment fragment = new AreaByPlaceListFragment();
        Bundle bundle = AreaByPlaceListPresenter.createArguments(scope, place);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public AreaByPlaceListPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaByPlaceListPresenter.View getViewInterface() {
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
    protected android.view.View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_area_by_place_list, container, false);
    }

    @Override
    protected void onBindView(@NonNull android.view.View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);

        recyclerView.setAdapter(new AreaByPlaceListAdapter(presenter));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ViewBindingFactory factory = new ViewBindingFactory(view);
        factory.create(R.id.button_previous, "onClick", presenter.commandBack).bind();
        factory.create(R.id.button_select, "onClick", presenter.commandSelect).bind();
    }

    @Override
    public void onDataSetChanged() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onAreaSelected(@NonNull Area area) {
        interactionListener.onAreaSelected(area);
    }

    @Override
    public void onBackView() {
        interactionListener.onBackToAreaFindView();
    }

    @Override
    public void onCloseView() {
        interactionListener.onCloseAreaByPlaceListView();
    }

    @Override
    public void onSnackbar(@StringRes int resId) {
        Snackbar.make(recyclerView, resId, Snackbar.LENGTH_SHORT).show();
    }

    public interface InteractionListener {

        void onAreaSelected(@NonNull Area area);

        void onBackToAreaFindView();

        void onCloseAreaByPlaceListView();
    }
}