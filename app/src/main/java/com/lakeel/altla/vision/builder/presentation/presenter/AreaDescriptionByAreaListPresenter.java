package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.StringProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.helper.AreaDescriptionNameComparater;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
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

public final class AreaDescriptionByAreaListPresenter
        extends BasePresenter<AreaDescriptionByAreaListPresenter.View> {

    private static final String ARG_SCOPE = "scrope";

    private static final String ARG_AREA = "area";

    @Inject
    VisionService visionService;

    public final RelayCommand commandClose = new RelayCommand(this::close);

    public final RelayCommand commandSelect = new RelayCommand(this::select, this::canSelect);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final List<AreaDescription> items = new ArrayList<>();

    private Scope scope;

    private Area area;

    private AreaDescription selectedAreaDescription;

    @Inject
    public AreaDescriptionByAreaListPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Scope scope, @NonNull Area area) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_SCOPE, Parcels.wrap(scope));
        bundle.putParcelable(ARG_AREA, Parcels.wrap(area));
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


        area = Parcels.unwrap(arguments.getParcelable(ARG_AREA));
        if (area == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' must be not null.", ARG_AREA));
        }
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();
        getView().onDataSetChanged();

        Disposable disposable = Single.<List<AreaDescription>>create(e -> {
            switch (scope) {
                case PUBLIC: {
                    visionService.getPublicAreaDescriptionApi()
                                 .findAreaDescriptionsByAreaId(area.getId(), areaDescriptions -> {
                                     Collections.sort(areaDescriptions, AreaDescriptionNameComparater.INSTANCE);
                                     e.onSuccess(areaDescriptions);
                                 }, e::onError);
                    break;
                }
                case USER: {
                    visionService.getUserAreaDescriptionApi()
                                 .findAreaDescriptionsByAreaId(area.getId(), areaDescriptions -> {
                                     Collections.sort(areaDescriptions, AreaDescriptionNameComparater.INSTANCE);
                                     e.onSuccess(areaDescriptions);
                                 }, e::onError);
                    break;
                }
            }
        }).subscribe(areaDescriptions -> {
            items.addAll(areaDescriptions);
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

    public int getItemCount() {
        return items.size();
    }

    @NonNull
    public ItemPresenter createItemPresenter() {
        return new ItemPresenter();
    }

    public void onItemSelected(int position) {
        if (0 <= position) {
            selectedAreaDescription = items.get(position);
        } else {
            selectedAreaDescription = null;
        }

        commandSelect.raiseOnCanExecuteChanged();
    }

    private void close() {
        getView().onCloseView();
    }

    private void select() {
        getView().onAreaDescriptionSelected(selectedAreaDescription);
        getView().onCloseView();
    }

    private boolean canSelect() {
        return selectedAreaDescription != null;
    }

    public interface View {

        void onDataSetChanged();

        void onAreaDescriptionSelected(@NonNull AreaDescription areaDescription);

        void onCloseView();

        void onSnackbar(@StringRes int resId);
    }

    public final class ItemPresenter {

        public final StringProperty propertyId = new StringProperty();

        public final StringProperty propertyName = new StringProperty();

        public void onBind(int position) {
            AreaDescription areaDescription = items.get(position);
            propertyId.set(areaDescription.getId());
            propertyName.set(areaDescription.getName());
        }
    }
}
