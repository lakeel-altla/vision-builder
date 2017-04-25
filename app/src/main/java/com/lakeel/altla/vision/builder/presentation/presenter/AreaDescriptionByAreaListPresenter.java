package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.helper.AreaDescriptionNameComparater;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaDescriptionByAreaListPresenter
        extends BasePresenter<AreaDescriptionByAreaListPresenter.View> {

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final List<AreaDescription> items = new ArrayList<>();

    private AreaDescription selectedItem;

    @Inject
    public AreaDescriptionByAreaListPresenter() {
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();
        getView().onDataSetChanged();

        final Scope scope = selectAreaSettingsModel.getAreaScope();
        final Area area = selectAreaSettingsModel.getArea();

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
            SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
        });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(ActionBarVisibleEvent.VISIBLE);
        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_area_description_by_area_list_view)));
        eventBus.post(HomeAsUpVisibleEvent.VISIBLE);
        eventBus.post(new HomeAsUpIndicatorEvent(resources.getDrawable(R.drawable.ic_arrow_back_white_24dp)));
    }

    @Override
    protected void onStopOverride() {
        super.onStopOverride();

        compositeDisposable.clear();
    }

    public void prepareOptionsMenu() {
        getView().setActionSelectEnabled(selectedItem != null);
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

        eventBus.post(InvalidateOptionsMenuEvent.INSTANCE);
    }

    public void select() {
        selectAreaSettingsModel.selectAreaDescriptiob(selectedItem);
        eventBus.post(new BackViewEvent(getView()));
    }

    public interface View {

        void onDataSetChanged();

        void setActionSelectEnabled(boolean enabled);
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
