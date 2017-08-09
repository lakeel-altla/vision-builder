package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.ThumbnailLoader;
import com.lakeel.altla.vision.builder.presentation.model.ImageAssetListModel;
import com.lakeel.altla.vision.builder.presentation.model.OnItemEventAdapter;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.model.Scope;

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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ImageAssetListPane extends Pane {

    private static final Log LOG = LogFactory.getLog(ImageAssetListPane.class);

    @Inject
    ImageAssetListModel imageAssetListModel;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private final PaneContext paneContext;

    private final Adapter adapter = new Adapter();

    private final OnItemEventAdapter onItemEventAdapter = new OnItemEventAdapter(adapter);

    public ImageAssetListPane(@NonNull Activity activity) {
        super(activity, R.id.pane_image_asset_list);

        ((ActivityScopeContext) activity).getActivityComponent().inject(this);

        paneContext = (PaneContext) activity;

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    protected void onShow() {
        super.onShow();

        imageAssetListModel.getQueryAdapter().setOnItemEventListener(onItemEventAdapter);
        // TODO: The public scope.
        imageAssetListModel.queryItems(Scope.USER);
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        paneContext.showEditModeMenuPane();
    }

    public interface PaneContext {

        void onImageAssetSelected(@Nullable ImageAsset asset);

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

                imageAssetListModel.setSelectedPosition(selectedPosition);
                paneContext.onImageAssetSelected(imageAssetListModel.getSelectedItem());
            });

            return new Adapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Adapter.ViewHolder holder, int position) {
            final ImageAsset imageAsset = imageAssetListModel.getQueryAdapter().getItem(position);
            holder.textViewName.setText(imageAsset.getName());

            imageAssetListModel.loadAssetFile(imageAsset.getId(), file -> {
                thumbnailLoader.load(file, holder.imageViewThumbnail);
            }, e -> {
                LOG.e("Failed.", e);
                // TODO: show an error icon as an alternative.
            }, null);
        }

        @Override
        public int getItemCount() {
            return imageAssetListModel.getQueryAdapter().getItemCount();
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
