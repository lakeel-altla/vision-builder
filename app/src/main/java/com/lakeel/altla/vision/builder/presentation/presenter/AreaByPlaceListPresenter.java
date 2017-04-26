package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.CloseAreaByPlaceListViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.model.Area;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaByPlaceListPresenter extends BasePresenter<AreaByPlaceListPresenter.View> {

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final List<Area> items = new ArrayList<>();

    private Area selectedItem;

    @Inject
    public AreaByPlaceListPresenter() {
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
        getView().notifyDataSetChanged();

        final Disposable disposable = selectAreaSettingsModel
                .loadAreasByPlace()
                .subscribe(areas -> {
                    items.addAll(areas);
                    getView().notifyDataSetChanged();
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
        eventBus.post(CloseAreaByPlaceListViewEvent.INSTANCE);
    }

    public interface View {

        void notifyDataSetChanged();

        void setActionSelectEnabled(boolean enabled);
    }

    public final class ItemPresenter {

        public final StringProperty propertyId = new StringProperty();

        public final StringProperty propertyName = new StringProperty();

        public final StringProperty propertyLevel = new StringProperty();

        public void onBind(int position) {
            final Area area = items.get(position);
            propertyId.set(area.getId());
            propertyName.set(area.getName());
            propertyLevel.set(String.valueOf(area.getLevel()));
        }
    }
}
