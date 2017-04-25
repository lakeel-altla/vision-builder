package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.AreaSettingsListPresenter;
import com.lakeel.altla.vision.builder.presentation.view.adapter.AreaSettingsListAdapter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class AreaSettingsListFragment
        extends AbstractFragment<AreaSettingsListPresenter.View, AreaSettingsListPresenter>
        implements AreaSettingsListPresenter.View {

    @Inject
    AreaSettingsListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Menu menu;

    @NonNull
    public static AreaSettingsListFragment newInstance() {
        return new AreaSettingsListFragment();
    }

    @Override
    protected AreaSettingsListPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected AreaSettingsListPresenter.View getViewInterface() {
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
    protected void onCreateViewOverride(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onCreateViewOverride(view, savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_area_settings_list, menu);
        this.menu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        presenter.prepareOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                presenter.select();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void notifyItemInserted(int position) {
        recyclerView.getAdapter().notifyItemInserted(position);
    }

    @Override
    public void notifyDataSetChanged() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void setActionSelectEnabled(boolean enabled) {
        menu.findItem(R.id.action_select).setEnabled(enabled);
    }
}
