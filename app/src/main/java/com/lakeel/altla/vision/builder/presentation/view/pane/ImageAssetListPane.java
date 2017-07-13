package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.ThumbnailLoader;
import com.lakeel.altla.vision.model.ImageAsset;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ImageAssetListPane extends Pane {

    private static final Log LOG = LogFactory.getLog(ImageAssetListPane.class);

    @Inject
    VisionService visionService;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private final PageContext pageContext;

    private final List<ImageAsset> items = new ArrayList<>();

    private final Adapter adapter = new Adapter();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ImageAsset selectedItem;

    public ImageAssetListPane(@NonNull Activity activity) {
        super(activity, R.id.pane_image_asset_list);

        ((ActivityScopeContext) activity).getActivityComponent().inject(this);

        pageContext = (PageContext) activity;

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    @Override
    protected void onShow() {
        super.onShow();

        items.clear();
        adapter.notifyDataSetChanged();

        final Disposable disposable = Single.<List<ImageAsset>>create(e -> {
            visionService.getUserAssetApi()
                         .findAllUserImageAssets(e::onSuccess, e::onError);
        }).subscribe(imageAssets -> {
            items.addAll(imageAssets);
            adapter.notifyDataSetChanged();
        }, e -> {
            LOG.e("Failed.", e);
        });
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onHide() {
        super.onHide();
        compositeDisposable.clear();
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        pageContext.showEditModeMenuPane();
    }

    private void onItemSelected(int position) {
        if (0 <= position) {
            selectedItem = items.get(position);
        } else {
            selectedItem = null;
        }

        pageContext.onImageAssetSelected(selectedItem);
    }

    public interface PageContext {

        void onImageAssetSelected(@Nullable ImageAsset imageAsset);

        void showEditModeMenuPane();
    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        final ThumbnailLoader thumbnailLoader = new ThumbnailLoader(activity);

        View selectedItemView;

        LayoutInflater inflater;

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            final View itemView = inflater.inflate(R.layout.item_image_asset, parent, false);
            itemView.setOnClickListener(v -> {
                if (selectedItemView != null) {
                    selectedItemView.setSelected(false);
                }

                selectedItemView = (selectedItemView == v) ? null : v;

                int selectedPosition = -1;
                if (selectedItemView != null) {
                    selectedItemView.setSelected(true);
                    selectedPosition = recyclerView.getChildAdapterPosition(selectedItemView);
                }

                onItemSelected(selectedPosition);
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            final ImageAsset imageAsset = items.get(position);
            holder.textViewName.setText(imageAsset.getName());

            final File file = visionService.getUserAssetApi().findUserImageAssetCacheFile(imageAsset.getId());
            if (file == null) {
                // First, download and cache the file if it is not cached.
                final Disposable disposable = Single.<File>create(e -> {
                    visionService.getUserAssetApi()
                                 .downloadUserImageAssetFile(imageAsset.getId(), e::onSuccess, e::onError, null);
                }).subscribe(f -> {
                    thumbnailLoader.load(f, holder.imageViewThumbnail);
                }, e -> {
                    LOG.e("Failed.", e);
                    // TODO: show an error icon as an alternative.
                });
                compositeDisposable.add(disposable);
            } else {
                // Load from the cached file.
                thumbnailLoader.load(file, holder.imageViewThumbnail);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        final class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.text_view_name)
            TextView textViewName;

            @BindView(R.id.image_view_thumbnail)
            ImageView imageViewThumbnail;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
