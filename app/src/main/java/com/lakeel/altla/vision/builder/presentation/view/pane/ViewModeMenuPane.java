package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.builder.presentation.view.activity.AreaSettingsActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.SettingsActivity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class ViewModeMenuPane extends Pane {

    @Inject
    ArModel arModel;

    private final PaneContext paneContext;

    public ViewModeMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_view_mode_menu);

        ((ActivityScopeContext) activity).getActivityComponent().inject(this);
        paneContext = (PaneContext) activity;

        ButterKnife.findById(view, R.id.image_button_switch_to_edit_mode)
                   .setVisibility(arModel.canEdit() ? VISIBLE : GONE);
    }

    @OnClick(R.id.image_button_show_area_settings)
    void onClickShowAreaSettings() {
        final Intent intent = AreaSettingsActivity.createIntent(activity);
        activity.startActivity(intent);
    }

    @OnClick(R.id.image_button_switch_to_edit_mode)
    void onClickSwitchToEditMode() {
        paneContext.showEditModeMenuPane();
    }

    @OnClick(R.id.image_button_show_settings)
    void onClickShowSettings() {
        final Intent intent = SettingsActivity.createIntent(activity);
        activity.startActivity(intent);
    }

    public interface PaneContext {

        void showEditModeMenuPane();
    }
}
