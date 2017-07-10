package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.vision.api.VisionService;
import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.helper.ObservableHelper;
import com.lakeel.altla.vision.builder.presentation.helper.ThumbnailLoader;
import com.lakeel.altla.vision.builder.presentation.model.ActorDragConstants;
import com.lakeel.altla.vision.helper.ObservableListEvent;
import com.lakeel.altla.vision.model.ImageAsset;
import com.lakeel.altla.vision.presentation.presenter.model.DataList;

import org.parceler.Parcels;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class ImageAssetListFragment extends Fragment implements DataList.OnItemListener {

    private static final Log LOG = LogFactory.getLog(ImageAssetListFragment.class);

    @Inject
    VisionService visionService;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private final DataList<Item> items = new DataList<>(this);

    private final Adapter adapter = new Adapter();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private FragmentContext fragmentContext;

    @NonNull
    public static ImageAssetListFragment newInstance() {
        return new ImageAssetListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((ActivityScopeContext) context).getActivityComponent().inject(this);
        fragmentContext = FragmentContext.class.cast(getParentFragment());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image_asset_list, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        items.clear();

        Disposable disposable = ObservableHelper
                .usingList(() -> visionService.getUserAssetApi().observeAllUserImageAssets())
                .map(Event::new)
                .subscribe(event -> {
                    items.change(event.type, event.item, event.previousId);
                }, e -> {
                    LOG.e("Failed.", e);
                    Toast.makeText(getContext(), R.string.toast_failed, Toast.LENGTH_LONG).show();
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    @Override
    public void onItemInserted(int position) {
        adapter.notifyItemInserted(position);
    }

    @Override
    public void onItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        adapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.image_button_close)
    void onClickClose() {
        // TODO
    }

    public interface FragmentContext {

    }

    final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        final ThumbnailLoader thumbnailLoader = new ThumbnailLoader(getContext());

        LayoutInflater inflater;

        @Override
        public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }

            final View itemView = inflater.inflate(R.layout.item_image_asset, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Item item = items.get(position);
            holder.textViewName.setText(item.asset.getName());

            final Disposable disposable = Single.<Uri>create(e -> {
                visionService.getUserAssetApi()
                             .getUserImageAssetFileUriById(item.asset.getId(), e::onSuccess, e::onError);
            }).subscribe(uri -> {
                thumbnailLoader.load(uri, holder.imageViewThumbnail);
            }, e -> {
                LOG.e("Failed.", e);
            });
            compositeDisposable.add(disposable);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.text_view_name)
            TextView textViewName;

            @BindView(R.id.image_view_thumbnail)
            ImageView imageViewThumbnail;

            private final View.DragShadowBuilder dragShadowBuilder;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);

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

            @OnLongClick(R.id.view_top)
            boolean onLongClickViewTop() {
                // TODO
//                cancelStartDrag();
                final Item item = items.get(getAdapterPosition());
                onStartDrag(item.asset);
                return true;
            }

            void onStartDrag(@NonNull ImageAsset asset) {
                Intent intent = new Intent();
                intent.setExtrasClassLoader(Parcels.class.getClassLoader());
                intent.putExtra(ActorDragConstants.INTENT_EXTRA_ASSET, Parcels.wrap(asset));
                ClipData clipData = ClipData.newIntent(ActorDragConstants.INTENT_LABEL, intent);
                imageViewThumbnail.startDrag(clipData, dragShadowBuilder, null, 0);
            }
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
