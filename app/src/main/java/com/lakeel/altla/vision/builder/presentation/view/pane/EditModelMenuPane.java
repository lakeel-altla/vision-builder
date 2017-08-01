package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class EditModelMenuPane extends Pane {

    @BindView(R.id.image_button_expand_add_actor_menu)
    ImageButton imageButtonExpandAddActorMenu;

    @BindView(R.id.view_group_add_actor_menu)
    ViewGroup viewGroupAddActorMenu;

    private final PaneContext paneContext;

    public EditModelMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_edit_mode_menu);

        paneContext = (PaneContext) activity;

        imageButtonExpandAddActorMenu.setSelected(false);
        viewGroupAddActorMenu.setVisibility(GONE);
    }

    @OnClick(R.id.image_button_switch_to_view_mode)
    void onClickSwitchToViewMode() {
        paneContext.showViewModeMenuPane();
    }

    @OnClick(R.id.image_button_expand_add_actor_menu)
    void onClickExpandAddActorMenu() {
        final boolean selected = !imageButtonExpandAddActorMenu.isSelected();
        imageButtonExpandAddActorMenu.setSelected(selected);
        viewGroupAddActorMenu.setVisibility(selected ? VISIBLE : GONE);
    }

    @OnClick(R.id.button_show_image_asset_list_pane)
    void onClickShowImageAssetListPane() {
        paneContext.showImageAssetListPane();
    }

    @OnClick(R.id.button_show_trigger_list_pane)
    void onClickShowTriggerListPane() {
        paneContext.showTriggerListPane();
    }

    public interface PaneContext {

        void showViewModeMenuPane();

        void showImageAssetListPane();

        void showTriggerListPane();
    }
}
