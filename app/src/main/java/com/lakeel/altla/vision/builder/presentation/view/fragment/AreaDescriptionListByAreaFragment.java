package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.AreaDescriptionListByAreaModel;
import com.lakeel.altla.vision.builder.presentation.model.AreaSettingsModel;
import com.lakeel.altla.vision.builder.presentation.model.OnItemEventAdapter;
import com.lakeel.altla.vision.model.AreaDescription;

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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class AreaDescriptionListByAreaFragment extends Fragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Inject
    AreaSettingsModel areaSettingsModel;

    @Inject
    AreaDescriptionListByAreaModel areaDescriptionListByAreaModel;

    private final Adapter adapter = new Adapter();

    private final OnItemEventAdapter onItemEventAdapter = new OnItemEventAdapter(adapter);

    private FragmentContext fragmentContext;

    @NonNull
    public static AreaDescriptionListByAreaFragment newInstance() {
        return new AreaDescriptionListByAreaFragment();
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
        final View view = inflater.inflate(R.layout.fragment_area_description_list_by_area, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(R.string.title_area_description_by_area_list_view);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        setHasOptionsMenu(true);

        final String areaId = areaSettingsModel.getAreaId();
        if (areaId == null) throw new IllegalStateException("No area is selected.");

        areaDescriptionListByAreaModel.getQueryAdapter().setOnItemEventListener(onItemEventAdapter);
        areaDescriptionListByAreaModel.queryItems(areaSettingsModel.getAreaScope(), areaId);
    }

    @Override
    public void onStop() {
        super.onStop();
        areaDescriptionListByAreaModel.getQueryAdapter().setOnItemEventListener(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_area_description_by_area_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_select).setEnabled(areaDescriptionListByAreaModel.canSelect());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                final AreaDescription areaDescription = areaDescriptionListByAreaModel.getSelectedItem();
                if (areaDescription == null) throw new IllegalStateException("No area description is selected.");
                areaSettingsModel.selectAreaDescription(areaDescription);
                fragmentContext.backView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        public final Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            final View itemView = inflater.inflate(R.layout.item_area_description, parent, false);
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

                areaDescriptionListByAreaModel.setSelectedPosition(selectedPosition);
                fragmentContext.invalidateOptionsMenu();
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            final AreaDescription areaDescription = areaDescriptionListByAreaModel.getQueryAdapter().getItem(position);
            holder.textViewId.setText(areaDescription.getId());
            holder.textViewName.setText(areaDescription.getName());
        }

        @Override
        public int getItemCount() {
            return areaDescriptionListByAreaModel.getQueryAdapter().getItemCount();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.text_view_id)
            TextView textViewId;

            @BindView(R.id.text_view_name)
            TextView textViewName;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
