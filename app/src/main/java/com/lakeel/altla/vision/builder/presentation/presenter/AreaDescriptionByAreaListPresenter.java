package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.model.AreaDescription;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaDescriptionByAreaListPresenter extends BasePresenter<AreaDescriptionByAreaListPresenter.View> {

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

        final Disposable disposable = selectAreaSettingsModel
                .loadAreaDescriptionsByArea()
                .subscribe(areaDescriptions -> {
                    items.addAll(areaDescriptions);
                    getView().onDataSetChanged();
                }, e -> {
                    getLog().e("Failed.", e);
                    getView().showSnackbar(R.string.snackbar_failed);
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

        void showSnackbar(@StringRes int resId);
    }

    public final class ItemPresenter {

        public final StringProperty propertyId = new StringProperty();

        public final StringProperty propertyName = new StringProperty();

        public void onBind(int position) {
            final AreaDescription areaDescription = items.get(position);
            propertyId.set(areaDescription.getId());
            propertyName.set(areaDescription.getName());
        }
    }
}
