package com.lakeel.altla.vision.builder.presentation.view.fragment;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.presenter.ImageAssetListPresenter;
import com.lakeel.altla.vision.builder.presentation.view.adapter.ImageAssetListAdapter;
import com.lakeel.altla.vision.presentation.view.fragment.AbstractFragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ImageAssetListFragment
        extends AbstractFragment<ImageAssetListPresenter.View, ImageAssetListPresenter>
        implements ImageAssetListPresenter.View {

    @Inject
    ImageAssetListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private InteractionListener interactionListener;

    @NonNull
    public static ImageAssetListFragment newInstance() {
        return new ImageAssetListFragment();
    }

    @Override
    protected ImageAssetListPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected ImageAssetListPresenter.View getViewInterface() {
        return this;
    }

    @Override
    protected void onAttachOverride(@NonNull Context context) {
        super.onAttachOverride(context);

        ActivityScopeContext.class.cast(context).getActivityComponent().inject(this);
        interactionListener = InteractionListener.class.cast(getParentFragment());
    }

    @Override
    protected void onDetachOverride() {
        super.onDetachOverride();

        interactionListener = null;
    }

    @Nullable
    @Override
    protected android.view.View onCreateViewCore(LayoutInflater inflater, @Nullable ViewGroup container,
                                                 @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_asset_list, container, false);
    }

    @Override
    protected void onBindView(@NonNull android.view.View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);

        recyclerView.setAdapter(new ImageAssetListAdapter(presenter, getContext()));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
    }

    @Override
    public void onItemInserted(int position) {
        recyclerView.getAdapter().notifyItemInserted(position);
    }

    @Override
    public void onItemChanged(int position) {
        recyclerView.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void onItemRemoved(int position) {
        recyclerView.getAdapter().notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onDataSetChanged() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onCloseView() {
        interactionListener.onUpdateAssetListVisible(false);
        interactionListener.onUpdateMainMenuVisible(true);
    }

    @Override
    public void onSnackbar(@StringRes int resId) {
        Snackbar.make(recyclerView, resId, Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.image_button_close)
    void onClickButtonClose() {
        presenter.onClickButtonClose();
    }

    public interface InteractionListener {

        void onUpdateAssetListVisible(boolean visible);

        void onUpdateMainMenuVisible(boolean visible);
    }
}
