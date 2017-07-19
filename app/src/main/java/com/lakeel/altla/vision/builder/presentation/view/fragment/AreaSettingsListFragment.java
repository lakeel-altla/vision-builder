package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.StringResourceHelper;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.model.AreaSettings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaSettingsListFragment extends Fragment {

    private static final Log LOG = LogFactory.getLog(AreaSettingsListFragment.class);

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private final List<AreaSettings> items = new ArrayList<>();

    private final Adapter adapter = new Adapter();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FragmentContext fragmentContext;

    private AreaSettings selectedItem;

    @NonNull
    public static AreaSettingsListFragment newInstance() {
        return new AreaSettingsListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
        fragmentContext = (FragmentContext) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_area_settings_list, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(R.string.title_area_settings_list_view);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        setHasOptionsMenu(true);

        items.clear();
        adapter.notifyDataSetChanged();

        final Disposable disposable = selectAreaSettingsModel
                .loadAreaSettings()
                .subscribe(areaSettings -> {
                    items.add(areaSettings);
                    adapter.notifyItemInserted(items.size() - 1);
                }, e -> {
                    LOG.e("Failed.", e);
                    Toast.makeText(getContext(), R.string.toast_failed, Toast.LENGTH_SHORT).show();
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_area_settings_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_select).setEnabled(selectedItem != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                selectAreaSettingsModel.selectAreaSettings(selectedItem);
                fragmentContext.backView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onItemSelected(int position) {
        if (0 <= position) {
            selectedItem = items.get(position);
        } else {
            selectedItem = null;
        }

        fragmentContext.invalidateOptionsMenu();
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void invalidateOptionsMenu();

        void backView();
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        LayoutInflater inflater;

        View selectedItemView;

        @Override
        public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            final View itemView = inflater.inflate(R.layout.item_area_settings, parent, false);
            itemView.setOnClickListener(v -> {
                if (selectedItemView != null) {
                    selectedItemView.setSelected(false);
                }

                selectedItemView = (selectedItemView == v) ? null : v;

                int selectedPosition = -1;
                if (selectedItemView != null) {
                    selectedItemView.setSelected(true);
                    selectedPosition = recyclerView.getChildAdapterPosition(selectedItemView);
                }

                onItemSelected(selectedPosition);
            });

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final AreaSettings item = items.get(position);

            holder.textViewUpdatedAt.setText(String.valueOf(item.getUpdatedAtAsLong()));
            holder.textViewAreaMode.setText(StringResourceHelper.resolveScopeStringResource(item.getAreaScopeAsEnum()));
            holder.textViewAreaName.setText(item.getAreaName());
            holder.textViewAreaDescriptionName.setText(item.getAreaDescriptionName());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.text_view_updated_at)
            TextView textViewUpdatedAt;

            @BindView(R.id.text_view_area_mode)
            TextView textViewAreaMode;

            @BindView(R.id.text_view_area_name)
            TextView textViewAreaName;

            @BindView(R.id.text_view_area_description_name)
            TextView textViewAreaDescriptionName;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
