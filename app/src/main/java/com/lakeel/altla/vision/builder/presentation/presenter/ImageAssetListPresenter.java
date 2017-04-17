package com.lakeel.altla.vision.builder.presentation.presenter;

import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.ObservableHelper;
import com.lakeel.altla.vision.helper.ObservableListEvent;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.presentation.presenter.BasePresenter;
import com.lakeel.altla.vision.presentation.presenter.model.DataList;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ImageAssetListPresenter extends BasePresenter<ImageAssetListPresenter.View>
        implements DataList.OnItemListener {

    private final DataList<Item> items = new DataList<>(this);

    @Inject
    VisionService visionService;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Disposable getUserImageAssetFileUriUseCaseDisposable;

    @Inject
    public ImageAssetListPresenter() {
    }

    @Override
    protected void onStartOverride() {
        super.onStartOverride();

        items.clear();

        Disposable disposable = ObservableHelper
                .usingList(() -> visionService.getUserAssetApi().observeAllUserImageAssets())
                .map(Event::new)
                .subscribe(event -> {
                    items.change(event.type, event.item, event.previousId);
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

    @Override
    protected void onPauseOverride() {
        super.onPauseOverride();

        cancelStartDrag();
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

    public void onClickButtonClose() {
        getView().onCloseView();
    }

    public interface View {

        void onItemInserted(int position);

        void onItemChanged(int position);

        void onItemRemoved(int position);

        void onItemMoved(int fromPosition, int toPosition);

        void onDataSetChanged();

        void onCloseView();

        void onSnackbar(@StringRes int resId);
    }

    public interface ItemView {

        void onUpdateName(@Nullable String name);

        void onUpdateThumbnail(@NonNull Uri uri);

        void onStartDrag(@NonNull ImageAsset asset);
    }

    public final class ItemPresenter {

        private ItemView itemView;

        public void onCreateItemView(@NonNull ItemView itemView) {
            this.itemView = itemView;
        }

        public void onBind(int position) {
            Item item = items.get(position);
            itemView.onUpdateName(item.asset.getName());

            Disposable disposable = Single.<Uri>create(e -> {
                visionService.getUserAssetApi()
                             .getUserImageAssetFileUriById(item.asset.getId(), e::onSuccess, e::onError);
            }).subscribe(uri -> {
                itemView.onUpdateThumbnail(uri);
            }, e -> {
                getLog().e("Failed.", e);
            });
            compositeDisposable.add(disposable);
        }

        public void onLongClickViewTop(int position) {
            cancelStartDrag();

            Item item = items.get(position);

            itemView.onStartDrag(item.asset);
        }
    }

    private void cancelStartDrag() {
        if (getUserImageAssetFileUriUseCaseDisposable != null) {
            getUserImageAssetFileUriUseCaseDisposable.dispose();
            getUserImageAssetFileUriUseCaseDisposable = null;
        }
    }

    private final class Event {

        final ObservableListEvent.Type type;

        final Item item;

        final String previousId;

        Event(@NonNull ObservableListEvent<ImageAsset> event) {
            type = event.getType();
            item = new Item(event.getData());
            previousId = event.getPreviousChildName();
        }
    }

    private final class Item implements DataList.Item {

        final ImageAsset asset;

        Item(@NonNull ImageAsset asset) {
            this.asset = asset;
        }

        @Override
        public String getId() {
            return asset.getId();
        }
    }
}
