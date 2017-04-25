package com.lakeel.altla.vision.builder.presentation.presenter;

import com.google.android.gms.location.places.Place;

import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.ArgumentNullException;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.helper.AreaNameComparater;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaByPlaceListPresenter extends BasePresenter<AreaByPlaceListPresenter.View> {

    private static final String ARG_PLACE_ID = "placeId";

    @Inject
    VisionService visionService;

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final List<Area> items = new ArrayList<>();

    private String placeId;

    private Area selectedItem;

    @Inject
    public AreaByPlaceListPresenter() {
    }

    @NonNull
    public static Bundle createArguments(@NonNull Place place) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PLACE_ID, place.getId());
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.onCreate(arguments, savedInstanceState);

        if (arguments == null) throw new ArgumentNullException("arguments");

        String placeId = arguments.getString(ARG_PLACE_ID);
        if (placeId == null) {
            throw new IllegalArgumentException(String.format("Argument '%s' is required.", ARG_PLACE_ID));
        }

        this.placeId = placeId;
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(ActionBarVisibleEvent.VISIBLE);
        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_area_by_place_list_view)));
        eventBus.post(HomeAsUpVisibleEvent.VISIBLE);
        eventBus.post(new HomeAsUpIndicatorEvent(resources.getDrawable(R.drawable.ic_arrow_back_white_24dp)));
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();
        getView().onDataSetChanged();

        Disposable disposable = Single.<List<Area>>create(e -> {
            switch (selectAreaSettingsModel.getAreaScope()) {
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
            SnackbarEventHelper.post(eventBus, R.string.snackbar_done);
        });
        compositeDisposable.add(disposable);
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
        selectAreaSettingsModel.selectArea(selectedItem);
        eventBus.post(CloseViewEvent.INSTANCE);
    }

    public interface View {

        void onDataSetChanged();

        void setActionSelectEnabled(boolean enabled);
    }

    public static final class CloseViewEvent {

        private static final CloseViewEvent INSTANCE = new CloseViewEvent();

        private CloseViewEvent() {
        }
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
