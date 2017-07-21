package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;

import android.app.Activity;
import android.support.annotation.NonNull;

import butterknife.OnClick;

public final class EditModelMenuPane extends Pane {

    private final PaneContext paneContext;

    public EditModelMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_edit_mode_menu);

        paneContext = (PaneContext) activity;
    }

    @OnClick(R.id.image_button_switch_to_view_mode)
    void onClickSwitchToViewMode() {
        paneContext.showViewModeMenuPane();
    }

    @OnClick(R.id.image_button_show_image_asset_list_pane)
    void onClickShowAssetManagerPane() {
        paneContext.showImageAssetListPane();
    }

    public interface PaneContext {

        void showViewModeMenuPane();

        void showImageAssetListPane();
    }
}
