package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.binding.command.RelayCommand;
import com.lakeel.altla.android.binding.property.IntProperty;
import com.lakeel.altla.android.binding.property.LongProperty;
import com.lakeel.altla.android.binding.property.StringProperty;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.StringResourceHelper;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.AreaSettings;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaSettingsListPresenter extends BasePresenter<AreaSettingsListPresenter.View> {

    @Inject
    VisionService visionService;

    public final RelayCommand commandClose = new RelayCommand(this::close);

    public final RelayCommand commandSelect = new RelayCommand(this::select, this::canSelect);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final List<Item> items = new ArrayList<>();

    private Item selectedItem;

    @Inject
    public AreaSettingsListPresenter() {
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();
        getView().onDataSetChanged();

        Disposable disposable = Single
                .<List<AreaSettings>>create(e -> {
                    visionService.getUserAreaSettingsApi()
                                 .findAllUserAreaSettings(e::onSuccess, e::onError);
                })
                .flatMapObservable(Observable::fromIterable)
                .map(Item::new)
                .concatMap(item -> {
                    return Observable.<Item>create(e -> {
                        String areaId = item.areaSettings.getAreaId();
                        if (areaId == null) {
                            throw new IllegalStateException("Field 'areaId' is null.");
                        }

                        switch (item.areaSettings.getAreaScopeAsEnum()) {
                            case PUBLIC:
                                visionService.getPublicAreaApi()
                                             .findAreaById(areaId, area -> {
                                                 if (area != null) {
                                                     item.area = area;
                                                     e.onNext(item);
                                                 }
                                                 e.onComplete();
                                             }, e::onError);
                                break;
                            case USER:
                                visionService.getUserAreaApi()
                                             .findAreaById(areaId, area -> {
                                                 if (area != null) {
                                                     item.area = area;
                                                     e.onNext(item);
                                                 }
                                                 e.onComplete();
                                             }, e::onError);
                                break;
                            default:
                                throw new IllegalStateException("Unknown area scope.");
                        }
                    });
                })
                .concatMap(item -> {
                    return Observable.<Item>create(e -> {
                        String areaDescriptionId = item.areaSettings.getAreaDescriptionId();
                        if (areaDescriptionId == null) {
                            throw new IllegalStateException("Field 'areaId' is null.");
                        }

                        switch (item.areaSettings.getAreaScopeAsEnum()) {
                            case PUBLIC:
                                visionService.getPublicAreaDescriptionApi()
                                             .findAreaDescriptionById(areaDescriptionId, areaDescription -> {
                                                 if (areaDescription != null) {
                                                     item.areaDescription = areaDescription;
                                                     e.onNext(item);
                                                 }
                                                 e.onComplete();
                                             }, e::onError);
                                break;
                            case USER:
                                visionService.getUserAreaDescriptionApi()
                                             .findAreaDescriptionById(areaDescriptionId, areaDescription -> {
                                                 if (areaDescription != null) {
                                                     item.areaDescription = areaDescription;
                                                     e.onNext(item);
                                                 }
                                                 e.onComplete();
                                             }, e::onError);
                                break;
                            default:
                                throw new IllegalStateException("Unknown area scope.");
                        }
                    });
                })
                .subscribe(item -> {
                    items.add(item);
                    getView().onItemInserted(items.size() - 1);
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
            selectedItem = items.get(position);
        } else {
            selectedItem = null;
        }

        commandSelect.raiseOnCanExecuteChanged();
    }

    private void close() {
        getView().onCloseView();
    }

    private void select() {
        getView().onAreaSettingsSelected(selectedItem.areaSettings, selectedItem.area, selectedItem.areaDescription);
        getView().onCloseView();
    }

    private boolean canSelect() {
        return selectedItem != null;
    }

    public interface View {

        void onItemInserted(int position);

        void onDataSetChanged();

        void onAreaSettingsSelected(@NonNull AreaSettings areaSettings,
                                    @NonNull Area area,
                                    @NonNull AreaDescription areaDescription);

        void onCloseView();

        void onSnackbar(@StringRes int resId);
    }

    public final class ItemPresenter {

        public final IntProperty propertyAreaMode = new IntProperty(
                StringResourceHelper.resolveScopeStringResource(Scope.PUBLIC));

        public final LongProperty propertyUpdatedAt = new LongProperty();

        public final StringProperty propertyAreaName = new StringProperty();

        public final StringProperty propertyAreaDescriptionName = new StringProperty();

        public void onBind(int position) {
            Item item = items.get(position);

            propertyAreaMode.set(StringResourceHelper.resolveScopeStringResource(
                    item.areaSettings.getAreaScopeAsEnum()));
            propertyUpdatedAt.set(item.areaSettings.getUpdatedAtAsLong());
            propertyAreaName.set(item.area.getName());
            propertyAreaDescriptionName.set(item.areaDescription.getName());
        }
    }

    private final class Item {

        final AreaSettings areaSettings;

        Area area;

        AreaDescription areaDescription;

        Item(@NonNull AreaSettings areaSettings) {
            this.areaSettings = areaSettings;
        }
    }
}
