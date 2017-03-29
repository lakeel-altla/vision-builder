package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsListPresenter;
import com.lakeel.altla.vision.builder.presentation.view.AreaSettingsListView;
import com.lakeel.altla.vision.builder.presentation.view.adapter.AreaSettingsListAdapter;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
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
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class AreaSettingsListFragment extends AbstractFragment<AreaSettingsListView, AreaSettingsListPresenter>
        implements AreaSettingsListView {

    @Inject
    AreaSettingsListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.button_select)
    Button buttonSelect;

    private InteractionListener interactionListener;

    @NonNull
    public static AreaSettingsListFragment newInstance() {
        return new AreaSettingsListFragment();
    }

    @Override
    protected AreaSettingsListPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaSettingsListView getViewInterface() {
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
        return inflater.inflate(R.layout.fragment_area_settings_list, container, false);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);

        recyclerView.setAdapter(new AreaSettingsListAdapter(presenter));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onUpdateButtonSelectEnabled(boolean enabled) {
        buttonSelect.setEnabled(enabled);
    }

    @Override
    public void onItemInserted(int position) {
        recyclerView.getAdapter().notifyItemInserted(position);
    }

    @Override
    public void onDataSetChanged() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                       @NonNull Area area,
                                       @NonNull AreaDescription areaDescription) {
        interactionListener.onAreaSettingsSelected(areaSettings, area, areaDescription);
    }

    @Override
    public void onCloseView() {
        interactionListener.onCloseAreaSettingsListView();
    }

    @Override
    public void onSnackbar(@StringRes int resId) {
        Snackbar.make(recyclerView, resId, Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_close)
    void onClickButtonClose() {
        presenter.onClickButtonClose();
    }

    @OnClick(R.id.button_select)
    void onClickButtonSelect() {
        presenter.onClickButtonSelect();
    }

    public interface InteractionListener {

        void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                    @NonNull Area area,
                                    @NonNull AreaDescription areaDescription);

        void onCloseAreaSettingsListView();
    }
}
