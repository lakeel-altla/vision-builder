package com.lakeel.altla.vision.builder.presentation.view.pane;

import com.lakeel.altla.vision.builder.R;
import com.lakeel.altla.vision.builder.presentation.di.ActivityScopeContext;
import com.lakeel.altla.vision.builder.presentation.model.ArModel;
import com.lakeel.altla.vision.builder.presentation.view.activity.AreaSettingsActivity;
import com.lakeel.altla.vision.builder.presentation.view.activity.SettingsActivity;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class ViewModeMenuPane extends Pane {

    @Inject
    ArModel arModel;

    @BindView(R.id.image_button_expand_options)
    ImageButton imageButtonExpandOptions;

    @BindView(R.id.view_group_options)
    ViewGroup viewGroupOptions;

    @BindView(R.id.button_switch_to_edit_mode)
    Button buttonSwitchToEditMode;

    private final PaneContext paneContext;

    public ViewModeMenuPane(@NonNull Activity activity) {
        super(activity, R.id.pane_view_mode_menu);

        ((ActivityScopeContext) activity).getActivityComponent().inject(this);
        paneContext = (PaneContext) activity;

        imageButtonExpandOptions.setSelected(false);
        viewGroupOptions.setVisibility(GONE);
        buttonSwitchToEditMode.setVisibility(arModel.canEdit() ? VISIBLE : GONE);
    }

    @OnClick(R.id.image_button_show_area_settings)
    void onClickShowAreaSettings() {
        final Intent intent = AreaSettingsActivity.createIntent(activity);
        activity.startActivity(intent);
    }

    @OnClick(R.id.image_button_expand_options)
    void onClickExpandOptions() {
        final boolean selected = !imageButtonExpandOptions.isSelected();
        imageButtonExpandOptions.setSelected(selected);
        viewGroupOptions.setVisibility(selected ? VISIBLE : GONE);
    }

    @OnClick(R.id.button_switch_to_edit_mode)
    void onClickSwitchToEditMode() {
        paneContext.showEditModeMenuPane();
    }

    @OnClick(R.id.button_show_settings)
    void onClickShowSettings() {
        final Intent intent = SettingsActivity.createIntent(activity);
        activity.startActivity(intent);
    }

    public interface PaneContext {

        void showEditModeMenuPane();
    }
}
