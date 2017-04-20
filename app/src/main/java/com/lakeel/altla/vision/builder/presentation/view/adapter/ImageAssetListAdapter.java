package com.lakeel.altla.vision.builder.presentation.view.adapter;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.helper.ThumbnailLoader;
import com.lakeel.altla.vision.builder.presentation.model.ActorDragConstants;
import com.lakeel.altla.vision.builder.presentation.presenter.ImageAssetListPresenter;
import com.lakeel.altla.vision.model.ImageAsset;

import org.parceler.Parcels;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

public final class ImageAssetListAdapter extends RecyclerView.Adapter<ImageAssetListAdapter.ViewHolderAsset> {

    private final ImageAssetListPresenter presenter;

    private final Context context;

    private final ThumbnailLoader thumbnailLoader;

    private LayoutInflater inflater;

    public ImageAssetListAdapter(@NonNull ImageAssetListPresenter presenter, @NonNull Context context) {
        this.presenter = presenter;
        this.context = context;
        thumbnailLoader = new ThumbnailLoader(context);
    }

    @Override
    public final ViewHolderAsset onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.getContext());
        }
        View itemView = inflater.inflate(R.layout.item_image_asset, parent, false);
        return new ViewHolderAsset(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolderAsset holder, int position) {
        holder.itemPresenter.onBind(position);
    }

    @Override
    public int getItemCount() {
        return presenter.getItemCount();
    }

    class ViewHolderAsset extends RecyclerView.ViewHolder implements ImageAssetListPresenter.ItemView {

        @BindView(R.id.text_view_name)
        TextView textViewName;

        @BindView(R.id.image_view_thumbnail)
        ImageView imageViewThumbnail;

        private ImageAssetListPresenter.ItemPresenter itemPresenter;

        private final View.DragShadowBuilder dragShadowBuilder;

        ViewHolderAsset(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemPresenter = presenter.createItemPresenter();
            itemPresenter.onCreateItemView(this);

            dragShadowBuilder = new View.DragShadowBuilder(imageViewThumbnail);

            imageViewThumbnail.setOnDragListener((view, dragEvent) -> {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // returns true to accept a drag event.
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        // does not accept to drop here.
                        return false;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }

                return false;
            });
        }

        @Override
        public void onUpdateName(@Nullable String name) {
            textViewName.setText(name);
        }

        @Override
        public void onUpdateThumbnail(@NonNull Uri uri) {
            thumbnailLoader.load(uri, imageViewThumbnail);
        }

        @Override
        public void onStartDrag(@NonNull ImageAsset asset) {
            Intent intent = new Intent();
            intent.setExtrasClassLoader(Parcels.class.getClassLoader());
            intent.putExtra(ActorDragConstants.INTENT_EXTRA_ASSET, Parcels.wrap(asset));
            ClipData clipData = ClipData.newIntent(ActorDragConstants.INTENT_LABEL, intent);
            imageViewThumbnail.startDrag(clipData, dragShadowBuilder, null, 0);
        }

        @OnLongClick(R.id.view_top)
        boolean onLongClickViewTop() {
            itemPresenter.onLongClickViewTop(getAdapterPosition());
            return true;
        }
    }
}
