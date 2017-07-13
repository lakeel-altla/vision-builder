package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;

import android.app.Activity;
import android.support.annotation.NonNull;

import butterknife.OnClick;

public final class EditModelMenuPane extends Pane {

    private final PageContext pageContext;

    public EditModelMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_edit_mode_menu);

        pageContext = (PageContext) activity;
    }

    @OnClick(R.id.image_button_switch_to_view_mode)
    void onClickSwitchToViewMode() {
        pageContext.showViewModeMenuPane();
    }

    @OnClick(R.id.image_button_show_image_asset_list_pane)
    void onClickShowAssetManagerPane() {
        pageContext.showImageAssetListPane();
    }

    public interface PageContext {

        void showViewModeMenuPane();

        void showImageAssetListPane();
    }
}
