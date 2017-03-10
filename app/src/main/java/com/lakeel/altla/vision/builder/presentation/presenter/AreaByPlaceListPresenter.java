package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.view.AreaByPlaceItemView;
import com.lakeel.altla.vision.builder.presentation.view.AreaByPlaceListView;
import com.lakeel.altla.vision.domain.model.Area;
import com.lakeel.altla.vision.domain.model.AreaScope;
import com.lakeel.altla.vision.domain.usecase.FindAreasByPlaceUseCase;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;
import com.lakeel.altla.vision.presentation.presenter.model.DataList;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public final class AreaByPlaceListPresenter extends BasePresenter<AreaByPlaceListView>
        implements DataList.OnItemListener {

    private static final String ARG_AREA_SCOPE_VALUE = "areaScopeValue";

    private final List<Area> items = new ArrayList<>();

    @Inject
    FindAreasByPlaceUseCase findAreasByPlaceUseCase;

    private AreaScope areaScope;

    @Inject
    public AreaByPlaceListPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull AreaScope areaScope) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_AREA_SCOPE_VALUE, areaScope.getValue());
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        int areaScopeValue = arguments.getInt(ARG_AREA_SCOPE_VALUE, -1);
        if (areaScopeValue < 0) {
            throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_AREA_SCOPE_VALUE));
        }

        areaScope = AreaScope.toAreaScope(areaScopeValue);
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();
        getView().onDataSetChanged();
    }

    @Override
    public void onItemInserted(int index) {
        getView().onItemInserted(index);
    }

    @Override
    public void onItemChanged(int index) {
        getView().onItemChanged(index);
    }

    @Override
    public void onItemRemoved(int index) {
        getView().onItemRemoved(index);
    }

    @Override
    public void onItemMoved(int from, int to) {
        getView().onItemMoved(from, to);
    }

    @Override
    public void onDataSetChanged() {
        getView().onDataSetChanged();
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public ItemPresenter createItemPresenter() {
        return new ItemPresenter();
    }

    public void onClickItem(int position) {
        Area area = items.get(position);
        getView().onItemSelected(area.getId());
    }

    public void onActionPickPlace() {
        getView().onShowPlacePicker();
    }

    public void onPlacePicked(@NonNull Place place) {
        // onPlacePicked will be invoked after Fragment#onStart() because of the result of startActivityForResult.
        items.clear();
        getView().onDataSetChanged();

        Disposable disposable = findAreasByPlaceUseCase
                .execute(areaScope, place.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(areas -> {
                    items.addAll(areas);
                    getView().onDataSetChanged();
                }, e -> {
                    getLog().e("Failed.", e);
                    getView().onSnackbar(R.string.snackbar_failed);
                });
        manageDisposable(disposable);
    }

    public final class ItemPresenter {

        private AreaByPlaceItemView itemView;

        public void onCreateItemView(@NonNull AreaByPlaceItemView itemView) {
            this.itemView = itemView;
        }

        public void onBind(int position) {
            Area area = items.get(position);
            itemView.onUpdateAreaId(area.getId());
            itemView.onUpdateName(area.getName());
            itemView.onUpdateLevel(area.getLevel());
        }
    }
}
