package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.android.property.IntProperty;
import com.lakeel.altla.android.property.LongProperty;
import com.lakeel.altla.android.property.StringProperty;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarTitleEvent;
import com.lakeel.altla.vision.builder.presentation.event.ActionBarVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.BackViewEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpIndicatorEvent;
import com.lakeel.altla.vision.builder.presentation.event.HomeAsUpVisibleEvent;
import com.lakeel.altla.vision.builder.presentation.event.InvalidateOptionsMenuEvent;
import com.lakeel.altla.vision.builder.presentation.helper.SnackbarEventHelper;
import com.lakeel.altla.vision.builder.presentation.helper.StringResourceHelper;
import com.lakeel.altla.vision.builder.presentation.model.SelectAreaSettingsModel;
import com.lakeel.altla.vision.model.Scope;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;

import org.greenrobot.eventbus.EventBus;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class AreaSettingsListPresenter extends BasePresenter<AreaSettingsListPresenter.View> {

    @Inject
    EventBus eventBus;

    @Inject
    Resources resources;

    @Inject
    SelectAreaSettingsModel selectAreaSettingsModel;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final List<SelectAreaSettingsModel.AreaSettingsDetail> items = new ArrayList<>();

    private SelectAreaSettingsModel.AreaSettingsDetail selectedItem;

    @Inject
    public AreaSettingsListPresenter() {
    }

    @Override
    protected void onCreateViewOverride() {
        super.onCreateViewOverride();

        eventBus.post(ActionBarVisibleEvent.VISIBLE);
        eventBus.post(new ActionBarTitleEvent(resources.getString(R.string.title_area_settings_list_view)));
        eventBus.post(HomeAsUpVisibleEvent.VISIBLE);
        eventBus.post(new HomeAsUpIndicatorEvent(resources.getDrawable(R.drawable.ic_arrow_back_white_24dp)));
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();
        getView().notifyDataSetChanged();

        final Disposable disposable = selectAreaSettingsModel
                .loadAreaSettingsDetails()
                .subscribe(detail -> {
                    items.add(detail);
                    getView().notifyItemInserted(items.size() - 1);
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
        selectAreaSettingsModel.selectAreaSettings(selectedItem.areaSettings,
                                                   selectedItem.area,
                                                   selectedItem.areaDescription);
        eventBus.post(new BackViewEvent(getView()));
    }

    public interface View {

        void notifyItemInserted(int position);

        void notifyDataSetChanged();

        void setActionSelectEnabled(boolean enabled);
    }

    public final class ItemPresenter {

        public final IntProperty propertyAreaMode = new IntProperty(
                StringResourceHelper.resolveScopeStringResource(Scope.PUBLIC));

        public final LongProperty propertyUpdatedAt = new LongProperty();

        public final StringProperty propertyAreaName = new StringProperty();

        public final StringProperty propertyAreaDescriptionName = new StringProperty();

        public void onBind(int position) {
            final SelectAreaSettingsModel.AreaSettingsDetail item = items.get(position);

            propertyAreaMode.set(StringResourceHelper.resolveScopeStringResource(
                    item.areaSettings.getAreaScopeAsEnum()));
            propertyUpdatedAt.set(item.areaSettings.getUpdatedAtAsLong());
            propertyAreaName.set(item.area.getName());
            propertyAreaDescriptionName.set(item.areaDescription.getName());
        }
    }
}
