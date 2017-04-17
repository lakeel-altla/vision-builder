package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.StringProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.helper.AreaNameComparater;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.parceler.Parcels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaByPlaceListPresenter extends BasePresenter<AreaByPlaceListPresenter.View> {

    private static final String ARG_SCOPE = "scope";

    private static final String ARG_PLACE_ID = "placeId";

    @Inject
    VisionService visionService;

    public final RelayCommand commandBack = new RelayCommand(this::back);

    public final RelayCommand commandSelect = new RelayCommand(this::select, this::canSelect);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final List<Area> items = new ArrayList<>();

    private ParentView parentView;

    private Scope scope;

    private String placeId;

    private Area selectedArea;

    @Inject
    public AreaByPlaceListPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Scope scope, @NonNull Place place) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SCOPE, Parcels.wrap(scope));
        bundle.putString(ARG_PLACE_ID, place.getId());
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        scope = Parcels.unwrap(arguments.getParcelable(ARG_SCOPE));
        if (scope == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_SCOPE));
        }

        String placeId = arguments.getString(ARG_PLACE_ID);
        if (placeId == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_PLACE_ID));
        }

        this.placeId = placeId;
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();
        getView().onDataSetChanged();

        Disposable disposable = Single.<List<Area>>create(e -> {
            switch (scope) {
                case PUBLIC: {
                    visionService.getPublicAreaApi().findAreasByPlaceId(placeId, areas -> {
                        Collections.sort(areas, AreaNameComparater.INSTANCE);
                        e.onSuccess(areas);
                    }, e::onError);
                    break;
                }
                case USER: {
                    visionService.getUserAreaApi().findAreasByPlaceId(placeId, areas -> {
                        Collections.sort(areas, AreaNameComparater.INSTANCE);
                        e.onSuccess(areas);
                    }, e::onError);
                    break;
                }
            }
        }).subscribe(areas -> {
            items.addAll(areas);
            getView().onDataSetChanged();
        }, e -> {
            getLog().e("Failed.", e);
            getView().onSnackbar(R.string.snackbar_failed);
        });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        compositeDisposable.clear();
    }

    public void onParentViewAttached(@NonNull ParentView parentView) {
        this.parentView = parentView;
    }

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public ItemPresenter createItemPresenter() {
        return new ItemPresenter();
    }

    public void onItemSelected(int position) {
        if (0 <= position) {
            selectedArea = items.get(position);
        } else {
            selectedArea = null;
        }

        commandSelect.raiseOnCanExecuteChanged();
    }

    private void back() {
        parentView.onBackToAreaFindView();
    }

    private void select() {
        parentView.onAreaSelected(selectedArea);
        parentView.onCloseAreaByPlaceListView();
    }

    private boolean canSelect() {
        return selectedArea != null;
    }

    public interface View {

        void onDataSetChanged();

        void onSnackbar(@StringRes int resId);
    }

    public interface ParentView {

        void onAreaSelected(@NonNull Area area);

        void onBackToAreaFindView();

        void onCloseAreaByPlaceListView();
    }

    public final class ItemPresenter {

        public final StringProperty propertyId = new StringProperty();

        public final StringProperty propertyName = new StringProperty();

        public final StringProperty propertyLevel = new StringProperty();

        public void onBind(int position) {
            Area area = items.get(position);
            propertyId.set(area.getId());
            propertyName.set(area.getName());
            propertyLevel.set(String.valueOf(area.getLevel()));
        }
    }
}
