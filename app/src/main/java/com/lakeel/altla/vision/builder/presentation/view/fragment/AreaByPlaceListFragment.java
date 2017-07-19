package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.AreaListByPlaceModel;
import com.lakeel.altla.vision.builder.presentation.model.OnItemEventAdapter;
import com.lakeel.altla.vision.builder.presentation.model.AreaSettingsModel;
import com.lakeel.altla.vision.model.Area;

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

public final class AreaByPlaceListFragment extends Fragment {

    @Inject
    AreaSettingsModel areaSettingsModel;

    @Inject
    AreaListByPlaceModel areaListByPlaceModel;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private final Adapter adapter = new Adapter();

    private final OnItemEventAdapter onItemEventAdapter = new OnItemEventAdapter(adapter);

    private FragmentContext fragmentContext;

    @NonNull
    public static AreaByPlaceListFragment newInstance() {
        return new AreaByPlaceListFragment();
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
        final View view = inflater.inflate(R.layout.fragment_area_by_place_list, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentContext.setTitle(R.string.title_area_by_place_list_view);
        fragmentContext.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        setHasOptionsMenu(true);

        final Place place = areaSettingsModel.getPlace();
        if (place == null) throw new IllegalStateException("No place is selected.");

        areaListByPlaceModel.getQueryAdapter().setOnItemEventListener(onItemEventAdapter);
        areaListByPlaceModel.queryItems(areaSettingsModel.getAreaScope(), place.getId());
    }

    @Override
    public void onStop() {
        super.onStop();
        areaListByPlaceModel.getQueryAdapter().setOnItemEventListener(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_area_by_place_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_select).setEnabled(areaListByPlaceModel.canSelect());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                final Area area = areaListByPlaceModel.getSelectedItem();
                if (area == null) throw new IllegalStateException("No area is selected.");
                areaSettingsModel.selectArea(area);
                fragmentContext.closeAreaByPlaceListView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface FragmentContext {

        void setTitle(@StringRes int resId);

        void setHomeAsUpIndicator(@DrawableRes int resId);

        void invalidateOptionsMenu();

        void closeAreaByPlaceListView();
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        LayoutInflater inflater;

        View selectedItemView;

        @Override
        public final Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            final View itemView = inflater.inflate(R.layout.item_area, parent, false);
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

                areaListByPlaceModel.setSelectedPosition(selectedPosition);
                fragmentContext.invalidateOptionsMenu();
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            final Area area = areaListByPlaceModel.getQueryAdapter().getItem(position);
            holder.textViewId.setText(area.getId());
            holder.textViewName.setText(area.getName());
            holder.textViewLevel.setText(String.valueOf(area.getLevel()));
        }

        @Override
        public int getItemCount() {
            return areaListByPlaceModel.getQueryAdapter().getItemCount();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.text_view_id)
            TextView textViewId;

            @BindView(R.id.text_view_name)
            TextView textViewName;

            @BindView(R.id.text_view_level)
            TextView textViewLevel;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}