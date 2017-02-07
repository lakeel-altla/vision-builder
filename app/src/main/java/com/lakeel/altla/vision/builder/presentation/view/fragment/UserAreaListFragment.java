package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.UserAreaListPresenter;
import com.lakeel.altla.vision.builder.presentation.view.UserAreaListView;
import com.lakeel.altla.vision.builder.presentation.view.adapter.UserAreaListAdapter;
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

public final class UserAreaListFragment extends AbstractFragment<UserAreaListView, UserAreaListPresenter>
        implements UserAreaListView {

    @Inject
    UserAreaListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private InteractionListener interactionListener;

    @NonNull
    public static UserAreaListFragment newInstance() {
        return new UserAreaListFragment();
    }

    @Override
    public UserAreaListPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected UserAreaListView getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
        interactionListener = InteractionListener.class.cast(context);
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
        return inflater.inflate(R.layout.fragment_user_area_list, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);

        recyclerView.setAdapter(new UserAreaListAdapter(presenter));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getActivity().setTitle(R.string.title_user_area_list);
    }

    @Override
    public void onItemInserted(int position) {
        recyclerView.getAdapter().notifyItemInserted(position);
    }

    @Override
    public void onItemsUpdated() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(String areaId) {
        interactionListener.onShowUserAreaDescriptionsInAreaView(areaId);
    }

    @Override
    public void onSnackbar(@StringRes int resId) {
        Snackbar.make(recyclerView, resId, Snackbar.LENGTH_SHORT).show();
    }

    public interface InteractionListener {

        void onShowUserAreaDescriptionsInAreaView(@NonNull String areaId);
    }
}